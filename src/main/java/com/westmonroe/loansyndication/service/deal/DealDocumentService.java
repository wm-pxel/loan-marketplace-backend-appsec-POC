package com.westmonroe.loansyndication.service.deal;

import com.westmonroe.loansyndication.dao.deal.DealDocumentDao;
import com.westmonroe.loansyndication.exception.InvalidDataException;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.DocumentCategory;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.service.DefinitionService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DealDocumentService {

    private static int MAX_VERSION = 20;

    private final DealDocumentDao dealDocumentDao;
    private final DefinitionService definitionService;

    public DealDocumentService(DealDocumentDao dealDocumentDao, DefinitionService definitionService) {
        this.dealDocumentDao = dealDocumentDao;
        this.definitionService = definitionService;
    }

    public DealDocument getDocumentForId(Long documentId) {
        return dealDocumentDao.findById(documentId);
    }

    public DealDocument getDocumentForExternalId(String documentExternalId) {
        return dealDocumentDao.findByExternalId(documentExternalId);
    }

    public List<DealDocument> getDocumentsForDeal(String dealUid) {
        return dealDocumentDao.findAllByDealUid(dealUid);
    }

    /**
     * This alternative save method is used for file upload endpoints.
     *
     * @param deal
     * @param categoryName
     * @param displayName
     * @param documentType
     * @param description
     * @param source
     * @param currentUser
     * @return
     */
    public DealDocument save(Deal deal, String categoryName, String displayName, String documentType, String description
                , String source, User currentUser) {

        String fileExtension = FilenameUtils.getExtension(displayName);
        String documentName = Instant.now().toEpochMilli() + "." + fileExtension;

        // Get the category to make sure that it's valid.  Do this first before the file is uploaded to the s3 bucket.
        DocumentCategory category = definitionService.getDocumentCategoryByName(categoryName);

        DealDocument document = new DealDocument();
        document.setDeal(deal);
        document.setDisplayName(displayName);
        document.setDocumentName(documentName);
        document.setCategory(category);
        document.setDocumentType(documentType);
        document.setDescription(description);
        document.setSource(source);
        document.setDocumentExternalId(UUID.randomUUID().toString());

        // Add the created by user to the deal document.
        document.setCreatedBy(currentUser);

        dealDocumentDao.save(document);

        return dealDocumentDao.findById(document.getId());
    }

    public DealDocument save(DealDocument document, User currentUser) {

        // Add the created by user to the deal document.
        document.setCreatedBy(currentUser);

        if ( document.getDocumentExternalId() == null ) {
            document.setDocumentExternalId(UUID.randomUUID().toString());
        }

        dealDocumentDao.save(document);

        return dealDocumentDao.findById(document.getId());
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * deal document fields that were sent.
     *
     * @param  documentMap  The map of requested fields and values to be updated.
     * @param  currentUser  The authenticated user.
     * @return dealDocument
     */
    public DealDocument update(Map<String, Object> documentMap, User currentUser) {

        //TODO: Verify that user has update permissions for this deal.

        if ( !( documentMap.containsKey("deal") && ((Map) documentMap.get("deal")).containsKey("uid") ) ) {
            throw new MissingDataException("The deal document must contain the deal uid for an update.");
        }

        if ( !documentMap.containsKey("id") ) {
            throw new MissingDataException("The deal document must contain the unique id for an update.");
        }

        // Get the deal document by the unique id.
        DealDocument dealDocument = dealDocumentDao.findById(Long.valueOf(documentMap.get("id").toString()));

        // Verify that the document belongs to the deal.
        if ( !dealDocument.getDeal().getUid().equals(((Map) documentMap.get("deal")).get("uid")) ) {
            throw new InvalidDataException("The document does not belong to the deal.");
        }

        /*
         * Check the fields in the map and update the deal document object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */
        if ( documentMap.containsKey("displayName") ) {
            dealDocument.setDisplayName((String) documentMap.get("displayName"));
        }

        if ( documentMap.containsKey("description") ) {
            dealDocument.setDescription((String) documentMap.get("description"));
        }

        // Add the updated by user to the deal document.
        dealDocument.setUpdatedBy(currentUser);

        // Update the deal document.
        dealDocumentDao.update(dealDocument);

        // Return the full deal document object.
        return dealDocumentDao.findById(dealDocument.getId());
    }

    public DealDocument update(DealDocument dealDocument) {
        dealDocumentDao.update(dealDocument);
        return dealDocumentDao.findById(dealDocument.getId());
    }

    public void deleteById(Long id) {
        dealDocumentDao.deleteById(id);
    }

    public void deleteAllByDealUid(String dealUid) {
        dealDocumentDao.deleteAllByDealUid(dealUid);
    }

    /**
     * This method returns a deal document that has been verified it belongs to the specified deal.
     *
     * @param documentId The document id.
     * @param dealUid    The deal uid.
     *
     * @return {@link DealDocument} - A deal document.
     */
    public DealDocument getValidDocumentForDeal(Long documentId, String dealUid) {

        DealDocument document = dealDocumentDao.findById(documentId);

        if ( !document.getDeal().getUid().equals(dealUid) ) {
            throw new InvalidDataException(String.format("The document does not belong to the deal (id = %d).", documentId));
        }

        return document;
    }

    /**
     * This method returns a list of Deal Documents that have been verified to belong to the specified deal.
     *
     * @param docList A list of deal documents.
     * @param dealUid The deal uid.
     *
     * @return {@link List}<{@link DealDocument}> A list of deal documents.
     */
    public List<DealDocument> getValidDocumentsForDeal(List<DealDocument> docList, String dealUid) {

        List<DealDocument> verifiedDocList = new ArrayList<>();

        for ( DealDocument doc : docList ) {

            DealDocument document = getValidDocumentForDeal(doc.getId(), dealUid);
            verifiedDocList.add(document);

        }

        return verifiedDocList;
    }

    /**
     * This method verifies that the display name is unique or it will generate a unique display name.  If there are
     * matches, the display name will have a version number appended as such "display name (#)".
     *
     * @param dealUid       The unique deal uid
     * @param displayName   The display name (file name) of the file to be uploaded.

     * @return displayName  A unique display name in the documents for the deal.
     */
    public String getUniqueFileName(String dealUid, String displayName) {

        // Remove any version from the display name before searching for a match.
        String baseName = FilenameUtils.getBaseName(displayName).replaceFirst("\\(\\d+\\)$", "").trim();
        String extension = FilenameUtils.getExtension(displayName);
        String searchName = baseName + "." + extension;             // format: "display name.ext"
        String wildCardName = baseName + " (%)." + extension;       // format: "display name (#).ext"

        // Get the list of documents with matching the search name or wild card name format.
        List<DealDocument> docs = dealDocumentDao.findAllByDealUidAndDisplayName(dealUid, searchName, wildCardName);

        // The default (if no matches) is the original displayName provided.
        String uniqueName = displayName;

        if ( !docs.isEmpty() ) {

            for ( int version = 0; version < MAX_VERSION; version++ ) {

                if ( version == 0 ) {
                    uniqueName = searchName;
                } else {
                    uniqueName = baseName + String.format(" (%d).", version) + extension;
                }

                // If document doesn't exist then this is the unique name to return.
                if ( !documentExistsInList(uniqueName, docs) ) {
                    break;
                }

            }

        }

        return uniqueName;
    }

    public DealDocument getDocumentByDealUidAndDisplayName(String dealUid, String displayName){
        return dealDocumentDao.findByDealUidAndDisplayName(dealUid, displayName);
    }

    private boolean documentExistsInList(String document, List<DealDocument> docs) {

        boolean exists = false;

        for ( DealDocument dd : docs ) {
            if ( dd.getDisplayName().equals(document) ) {
                exists = true;
                break;
            }
        }

        return exists;
    }

}