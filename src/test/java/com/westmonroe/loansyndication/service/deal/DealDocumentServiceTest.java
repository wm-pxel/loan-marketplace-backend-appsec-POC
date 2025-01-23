package com.westmonroe.loansyndication.service.deal;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.service.InstitutionService;
import com.westmonroe.loansyndication.service.UserService;
import com.westmonroe.loansyndication.utils.ModelUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class DealDocumentServiceTest {

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private DealService dealService;

    @Autowired
    private UserService userService;

    @Autowired
    private DealDocumentService dealDocumentService;

    @Test
    void givenNoDocumentsInDatabase_whenGettingAllForDeal_thenReturnEmptySet() {

        List<DealDocument> documents = dealDocumentService.getDocumentsForDeal(TEST_DEAL_UUID_1);
        assertThat(documents).isEmpty();
    }

    @Test
    void givenNoDocumentsInDatabase_whenGettingById_thenVerifyException() {

        assertThatThrownBy(() -> dealDocumentService.getDocumentForId(99L))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Transactional
    void givenNewDocuments_whenPerformingCruOperations_thenVerify() {

        // Create the current user object.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        // Create a test institution and stage for the deal.
        Institution institution = institutionService.save(ModelUtil.createTestInstitution(1));

        Deal deal = ModelUtil.createTestDeal(1);
        deal.setOriginator(institution);
        dealService.save(institution.getUid(), deal, currentUser, SYSTEM_MARKETPLACE);

        DealDocument document1 = ModelUtil.createTestDealDocument(deal, "display 1", "document 1", "desc 1");
        DealDocument document2 = ModelUtil.createTestDealDocument(deal, "display 2", "document 2", "desc 2");

        // Save two deal documents.
        DealDocument saved1 = dealDocumentService.save(document1, currentUser);
        DealDocument saved2 = dealDocumentService.save(document2, currentUser);

        // Verify the saved deal document.
        assertThat(saved1).isNotNull();
        assertThat(saved1.getId()).isNotNull();
        assertThat(saved1.getDeal().getUid()).isEqualTo(deal.getUid());
        assertThat(saved1.getDisplayName()).isEqualTo(document1.getDisplayName());
        assertThat(saved1.getDocumentName()).isEqualTo(document1.getDocumentName());
        assertThat(saved1.getCategory().getName()).isEqualTo("Collateral");
        assertThat(saved1.getDescription()).isEqualTo(document1.getDescription());
        assertThat(saved1.getSource()).isEqualTo(document1.getSource());
        assertThat(saved1.getCreatedBy().getUid()).isEqualTo(currentUser.getUid());

        // Get the number of documents for the deal.
        List<DealDocument> documents = dealDocumentService.getDocumentsForDeal(deal.getUid());

        // Verify the number of documents for the deal.
        assertThat(documents)
            .isNotNull()
            .hasSize(2);

        // Delete the first document.
        dealDocumentService.deleteById(saved1.getId());

        // Get the updated list of documents for the deal.
        documents = dealDocumentService.getDocumentsForDeal(deal.getUid());

        // Verify the number of documents for the deal.
        assertThat(documents)
            .isNotNull()
            .hasSize(1);
        assertThat(documents.get(0).getDisplayName()).isEqualTo(document2.getDisplayName());
        assertThat(documents.get(0).getDocumentName()).isEqualTo(document2.getDocumentName());
        assertThat(documents.get(0).getDescription()).isEqualTo(document2.getDescription());
        assertThat(documents.get(0).getSource()).isEqualTo(document2.getSource());

        // Delete all of the documents for the deal.
        dealDocumentService.deleteAllByDealUid(deal.getUid());

        // Get the updated list of documents for the deal.
        documents = dealDocumentService.getDocumentsForDeal(deal.getUid());

        // Verify the number of documents for the deal.
        assertThat(documents)
            .isNotNull()
            .isEmpty();
    }

    @Test
    @Transactional
    void givenDealDocuments_whenGettingUniqueDisplayName_thenVerify() {

        /*
         *  Test Deal: Kentucky Processing Plant
         */
        String displayName = "unique file name.txt";
        String expectedDisplayName = displayName;

        /*
         *  Case 1: There are no matches and the display name is unique. (happy path)
         */
        String uniqueDisplayName = dealDocumentService.getUniqueFileName(TEST_DEAL_UUID_2, displayName);
        assertThat(uniqueDisplayName).isEqualTo(expectedDisplayName);

        /*
         *  Case 2: There is only one match for the file name.
         */
        displayName = "2021 Financials - Smith Peanuts.xlsx";
        expectedDisplayName = "2021 Financials - Smith Peanuts (1).xlsx";

        uniqueDisplayName = dealDocumentService.getUniqueFileName(TEST_DEAL_UUID_2, displayName);
        assertThat(uniqueDisplayName).isEqualTo(expectedDisplayName);

        /*
         *  Case 3: There are three versions (base, 1 and 3).  Assume version 2 was renamed or deleted.
         */

        // Get the user object for current user.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_3);            // Leon T. (Tim) Amerson

        // Get the deal object.
        Deal deal = dealService.getDealByUid(TEST_DEAL_UUID_2, currentUser);  // Peanut Farming and Processing

        // Create and insert the first test document.
        DealDocument document = ModelUtil.createTestDealDocument(deal, "2021 Financials - Smith Peanuts (1).xlsx", "12345.xlsx", "");
        dealDocumentService.save(document, currentUser);

        // Create and insert the second test document.
        document = ModelUtil.createTestDealDocument(deal, "2021 Financials - Smith Peanuts (3).xlsx", "67890.xlsx", "");
        dealDocumentService.save(document, currentUser);

        displayName = "2021 Financials - Smith Peanuts.xlsx";
        expectedDisplayName = "2021 Financials - Smith Peanuts (2).xlsx";

        uniqueDisplayName = dealDocumentService.getUniqueFileName(TEST_DEAL_UUID_2, displayName);
        assertThat(uniqueDisplayName).isEqualTo(expectedDisplayName);
    }

}