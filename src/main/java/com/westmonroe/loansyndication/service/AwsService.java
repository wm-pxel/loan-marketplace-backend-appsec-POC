package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.exception.AwsCognitoException;
import com.westmonroe.loansyndication.exception.AwsS3Exception;
import com.westmonroe.loansyndication.exception.FileUploadException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.utils.DocumentCategoryEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetIdentityProviderByIdentifierRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetIdentityProviderByIdentifierResponse;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class AwsService {
    @Value("${lamina.environment}")
    private String environment;

    @Value("${s3.file.upload.bucket}")
    private String bucketName;

    @Value("${lamina.base-url}")
    private String baseUrl;

    @Value("${lamina.lambda.enabled}")
    private boolean lambdaEnabled;

    @Value("${lamina.lambda.dp-function-name}")
    private String documentProcessingFunctionName;

    @Value("${lamina.lambda.dd-function-name}")
    private String documentDistributionFunctionName;

    @Value("${lamina.cognito.user-pool-id}")
    private String userPoolId;

    private final S3Client s3Client;
    private final LambdaAsyncClient lambdaAsyncClient;

    private final CognitoIdentityProviderClient cognitoIdpClient;


    public AwsService(S3Client s3Client, LambdaAsyncClient lambdaAsyncClient, CognitoIdentityProviderClient cognitoIdpClient) {
        this.s3Client = s3Client;
        this.lambdaAsyncClient = lambdaAsyncClient;
        this.cognitoIdpClient = cognitoIdpClient;
    }

    public ListObjectsResponse getDocumentsForDeal(String dealUid) {

        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(bucketName)
                .marker(dealUid)
                .build();

        // Get the list of objects in the bucket and deal subfolder.
        ListObjectsResponse result = s3Client.listObjects(listObjects);

        if ( result == null ) {
            log.error("There were no objects found in the bucket or folder.");
            throw new AwsS3Exception("There were no objects found in the bucket or folder.");
        }

        return result;
    }

    public void uploadDocument(Deal deal, DealDocument document, User currentUser, MultipartFile multipartFile) {

        // Get the Document Category Enum based on the category name.
        DocumentCategoryEnum categoryEnum = DocumentCategoryEnum.valueOfName(document.getCategory().getName());

        // Check if the file is empty.
        if ( multipartFile.isEmpty() ) {
            log.error("The file to upload was empty.");
            throw new AwsS3Exception("The file to upload was empty.");
        }

        // Check if the file is valid.
        if ( Objects.isNull(document.getDisplayName()) || document.getDisplayName().trim().equals("") ) {
            log.error("The upload file is not valid.");
            throw new AwsS3Exception("The upload file is not valid.");
        }

        /*
         *  Verify the file is one of the supported types.  Verify by file extension.
         */
        String extension = FilenameUtils.getExtension(document.getDisplayName());
        if ( extension == null || !categoryEnum.getSupportedFileExtensions().contains(extension) ) {
            log.error(String.format("File type is not supported. (type = %s, user = %s)"
                    , extension, currentUser.getUid()));
            throw new FileUploadException("File type is not supported.");
        }

        /*
         *  Verify the file does not exceed the max size.
         */
        long fileSizeInBytes = multipartFile.getSize();
        long maxSizeInBytes = categoryEnum.getMaxFileSize() * 1024 * 1024;

        if ( fileSizeInBytes > maxSizeInBytes ) {
            log.error(String.format("File size exceeded maximum limit of %d MB. (size in bytes = %d, user = %s)"
                    , categoryEnum.getMaxFileSize(), fileSizeInBytes, currentUser.getUid()));
            throw new FileUploadException(String.format("File size exceeded maximum limit of %d MB.", categoryEnum.getMaxFileSize()));
        }

        /*
         *  Convert multipart file to a file
         */
        File file = new File(document.getDisplayName());

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)){
            fileOutputStream.write(multipartFile.getBytes());
        } catch ( IOException e ) {
            log.error("There was an error converting the multipart file to a file.");
            throw new AwsS3Exception("There was an error converting the multipart file to a file.", e);
        }

        // Create the metadata map for the file.
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("Content-Type", document.getDocumentType());
        metadataMap.put("Content-Length", String.valueOf(file.length()));
        metadataMap.put("s3-Filename", document.getDocumentName());
        metadataMap.put("Orig-Filename", document.getDisplayName());
        metadataMap.put("Category", document.getCategory().getName());
        metadataMap.put("Source", document.getSource());

        // Upload to s3 bucket.
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(deal.getUid() + "/" + document.getDocumentName())
                .metadata(metadataMap)
                .build();
        s3Client.putObject(request, file.toPath());

        // Delete the file
        file.delete();
    }

    public byte[] getDocumentContents(DealDocument document) throws IOException {

        String fileName = document.getDeal().getUid() + "/" + document.getDocumentName();
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(fileName)
                .bucket(bucketName)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);

        return objectBytes.asByteArray();
    }

    public void deleteDocument(DealDocument document) {

        List<ObjectIdentifier> toDelete = new ArrayList<>();
        toDelete.add(ObjectIdentifier.builder()
                .key(document.getDeal().getUid() + "/" + document.getDocumentName())
                .build());

        try {

            // Delete the document from the s3 bucket.
            DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                            .bucket(bucketName)
                            .delete(Delete.builder()
                                .objects(toDelete)
                                .build())
                            .build();
            s3Client.deleteObjects(request);

        } catch ( S3Exception | SdkClientException e ) {

            log.error(Arrays.toString(e.getStackTrace()));
            throw new AwsS3Exception("There was an error deleting the document from the s3 bucket.", e);

        }
    }

    public String getIdentityProviderForEmail(String email){
        String emailDomain = email.substring(email.indexOf("@")+1).toLowerCase();

        try {
            GetIdentityProviderByIdentifierRequest request = GetIdentityProviderByIdentifierRequest
                    .builder()
                    .idpIdentifier(emailDomain)
                    .userPoolId(userPoolId)
                    .build();

            GetIdentityProviderByIdentifierResponse response = cognitoIdpClient.getIdentityProviderByIdentifier(request);
            return response.identityProvider().providerName();
        } catch (Exception ex) {
            log.error(String.format("IDp Exception: Error retrieving idp for identifier: \"%s\"", email));
            log.error(ex.toString());
            throw new AwsCognitoException("Error retrieving idp for identifier", ex);
        }
    }

    /**
     * This method takes a list of deal documents to be zipped into one file and creates a ByteArrayOutputStream
     * of the zip file for download.
     *
     * @param documents     List of {@link DealDocument}
     * @return {@link ByteArrayOutputStream}
     * @throws IOException
     */
    public ByteArrayOutputStream createZipFileForDocuments(List<DealDocument> documents) throws IOException {

        // Create a ByteArrayOutputStream to hold the zipped file content
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(baos);

        for ( DealDocument document : documents ) {

            String s3FileName = document.getDeal().getUid() + "/" + document.getDocumentName();

            // Get the document from the s3 bucket.
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3FileName)
                    .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(request);

            zipOutputStream.putNextEntry(new ZipEntry(document.getDisplayName()));
            IOUtils.copy(objectBytes.asInputStream(), zipOutputStream);
            zipOutputStream.closeEntry();

        }

        // Close the ZipOutputStream
        zipOutputStream.close();

        return baos;
    }

    /**
     * This method makes an async call to the AWS Lambda function that will process the deal documents.  This method
     * should be called immediately after a deal is created from the integration, assuming it staged documents to be
     * processed.
     *
     * @param institutionUid    The lead/originating institution's uid.
     * @param batchId           The id for the batch of documents staged for the deal.
     */
    public void loadDealDocuments(String institutionUid, Long batchId) {
        String payload = "{"
                    + "\"baseUrl\": \"" + baseUrl + "\", "
                    + "\"environment\": \"" + environment + "\", "
                    + "\"institutionUid\": \"" + institutionUid + "\", "
                    + "\"batchId\": " + batchId + " "
                + "}";

        invokeLambdaFunction(documentProcessingFunctionName, payload);
    }

    public void distributeDealDocuments(String institutionUid, Long batchId) {
        String payload = "{"
                + "\"baseUrl\": \"" + baseUrl + "\", "
                + "\"institutionUid\": \"" + institutionUid + "\", "
                + "\"batchId\": " + batchId + " "
                + "}";

        invokeLambdaFunction(documentDistributionFunctionName, payload);
    }

    /**
     * This is a helper method for invoking AWS Lambda functions asynchronously.
     *
     * @param functionName  The Lambda function name to invoke.
     * @param payload       The payload sent to the AWS Lambda function.
     */
    private void invokeLambdaFunction(String functionName, String payload) {

        if ( lambdaEnabled ) {

            InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(SdkBytes.fromUtf8String(payload))
                    .build();

            lambdaAsyncClient.invoke(request);

        }
    }

}