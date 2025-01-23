package com.westmonroe.loansyndication.service.integration;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.integration.DocumentBatch;
import com.westmonroe.loansyndication.model.integration.DocumentBatchDetail;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@Testcontainers
class DocumentBatchServiceTest {

    @Autowired
    private DocumentBatchService documentBatchService;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void givenNewDocumentBatch_whenSavingAndDRetrievingData_thenVerify() {

        String dealExternalId = "b86517b4-0693-4ec6-b880-06de4c0507f3";         // Kentucky Processing Plant
        String documentExternalId = "e2b98259-b36b-4534-aabe-92193d3fd4e9";

        // Define date format for the version external id field.
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        // Make the current user Jane Halverson for the test.
        User currentUser = new User(12L);

        // Create the document batch object.  Note that the (deal) deal external id has to exist in the database.
        DocumentBatch batch = new DocumentBatch();
        batch.setDealExternalId(dealExternalId);

        // Create list of Document Batch Detail objects.
        List<DocumentBatchDetail> details = new ArrayList<>();

        DocumentBatchDetail documentBatchDetail = new DocumentBatchDetail();
        documentBatchDetail.setDocumentExternalId("a0e6369d-ac15-488e-a45a-47919231e3a1");
        documentBatchDetail.setUrl("https://wmpfinserv--lmdev.my.salesforce.com/services/data/v56.0/sobjects/ContentVersion/0688C000001GQhpQAG/VersionData");
        documentBatchDetail.setDisplayName("TestDocument1");
        documentBatchDetail.setExtension("pdf");
        documentBatchDetail.setCategory("Financials");
        details.add(documentBatchDetail);

        documentBatchDetail = new DocumentBatchDetail();
        documentBatchDetail.setDocumentExternalId(documentExternalId);
        documentBatchDetail.setUrl("https://wmpfinserv--lmdev.my.salesforce.com/services/data/v56.0/sobjects/ContentVersion/0688C000001GQhpQAG/VersionData");
        documentBatchDetail.setDisplayName("TestDocument2");
        documentBatchDetail.setExtension("docx");
        documentBatchDetail.setCategory("Collateral");
        details.add(documentBatchDetail);

        documentBatchDetail = new DocumentBatchDetail();
        documentBatchDetail.setDocumentExternalId("3af08b95-c04d-49ef-b720-d82fa60c9ffa");
        documentBatchDetail.setUrl("https://wmpfinserv--lmdev.my.salesforce.com/services/data/v56.0/sobjects/ContentVersion/0688C000001GQhpQAG/VersionData");
        documentBatchDetail.setDisplayName("TestDocument3");
        documentBatchDetail.setExtension("jpg");
        documentBatchDetail.setCategory("Entity Documents");
        details.add(documentBatchDetail);

        // Add the details to the batch.
        batch.setDetails(details);
        batch.setTransferType("U");

        DocumentBatch savedBatch = documentBatchService.save(batch, currentUser);

        // Verify all detail records were saved.
        assertThat(savedBatch.getDetails())
            .isNotNull()
            .hasSize(3);

        // Verify every document batch detail record.
        batch.getDetails().stream().forEach(detail -> {

            // Get the associated saved document batch detail record.
            DocumentBatchDetail savedDetail = savedBatch.getDetails().stream()
                .filter(d -> d.getDocumentExternalId().equals(detail.getDocumentExternalId()))
                .findFirst()
                .orElseThrow(DataNotFoundException::new);

            assertThat(detail)
                .hasFieldOrPropertyWithValue("documentExternalId", savedDetail.getDocumentExternalId())
                .hasFieldOrPropertyWithValue("url", savedDetail.getUrl())
                .hasFieldOrPropertyWithValue("displayName", savedDetail.getDisplayName())
                .hasFieldOrPropertyWithValue("extension", savedDetail.getExtension())
                .hasFieldOrPropertyWithValue("category", savedDetail.getCategory());
        });
    }

}