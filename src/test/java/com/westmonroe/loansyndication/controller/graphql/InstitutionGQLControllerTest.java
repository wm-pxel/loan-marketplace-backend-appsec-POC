package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.dao.InstitutionDao;
import com.westmonroe.loansyndication.dao.UserDao;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.security.WithMockJwtUser;
import com.westmonroe.loansyndication.utils.Constants;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.GraphQLUtil.insertTestInstitution;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureHttpGraphQlTester
@Testcontainers
class InstitutionGQLControllerTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenAllInstitutionsRequest_whenUsingDealViewerUser_thenVerifyAccessDenied() {

        /*
         *  This user is only a ACCESS_ALL_INST_DEALS and only a SUPER_ADM can access this endpoint.
         */
        graphQlTester
            .document("""
            query {
                allInstitutions {
                    uid
                    name
                    brandName
                    active
                }
            }
            """)
            .execute()
            .errors()
            .satisfy(error -> {
                assertThat(error)
                    .isNotNull()
                    .isNotEmpty();
                assertThat(error.get(0).getExtensions())
                    .containsEntry(Constants.GQL_CLASSIFICATION, "AuthorizationDeniedException");
                assertThat(error.get(0).getMessage()).isEqualTo("Access Denied");
            });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenExistingDealEventParticipants_whenGettingInstitutionsNotOnTheDeal_thenVerify() {

        List<Institution> institutions = graphQlTester
            .document("""
            query getEventParticipantsNotOnDeal($dealUid: String!) {
                getEventParticipantsNotOnDeal(dealUid: $dealUid) {
                    uid
                    name
                    brandName
                    active
                }
            }
            """)
            .variable("dealUid", TEST_DEAL_UUID_2)
            .execute()
            .path("getEventParticipantsNotOnDeal")
            .entityList(Institution.class)
            .get();

        assertThat(institutions)
            .isNotNull()
            .hasSize(3);
        assertThat(institutions.get(0))
            .hasFieldOrPropertyWithValue("name", "Farm Credit Bank of Texas");
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewInstitutionList_whenPerformingInserts_thenVerifySize() {

        // Perform the GraphQL mutation for inserting three institutions.
        insertTestInstitution(graphQlTester, "Institution 1", "Brand 1", "Y");
        insertTestInstitution(graphQlTester, "Institution 2", "Brand 2", "Y");
        insertTestInstitution(graphQlTester, "Institution 3", "Brand 3", "Y");

        graphQlTester
            .document("""
                query {
                    allInstitutions {
                        uid
                        name
                        brandName
                        active
                    }
                }
                """)
            .execute()
            .path("allInstitutions")
            .entityList(Institution.class)
            .satisfies(institutions -> {
                assertThat(institutions)
                    .isNotNull()
                    .hasSize(7);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewInstitution_whenPerformingCrudOperations_thenVerify() {

        String name = "Test Institution";
        String brandName = "Test Brand Name";
        String active = "Y";

        // Perform the GraphQL mutation for inserting an institution.
        Map<String, Object> institutionMap = insertTestInstitution(graphQlTester, name, brandName, active);

        assertThat(institutionMap)
            .isNotNull()
            .containsEntry("name", name)
            .containsEntry("brandName", brandName)
            .containsEntry("dealCount", 0)
            .containsEntry("memberCount", 0)
            .containsEntry("active", active);

        /*
         *  Need to insert a user with the specific UUID because of our test token.
         */
        User user = new User();
        user.setInstitution(institutionDao.findByUid((String) institutionMap.get("uid")));
        user.setUid(TEST_NEW_USER_UUID);
        user.setFirstName("Test");
        user.setLastName("Tester");
        user.setEmail("test.tester@westmonroe.com");
        user.setPassword("jhc9238dh9283dhd9283ncnSHY98129809");
        user.setActive("Y");

        userDao.save(user);

        // Get the institution by its UID.
        LinkedHashMap resultMap = graphQlTester
            .document("""
                query getInstitutionByUid($uid: String!) {
                    getInstitutionByUid(uid: $uid) {
                        uid
                        name
                        brandName
                        active
                    }
                }
                """)
            .variable("uid", institutionMap.get("uid"))
            .execute()
            .path("getInstitutionByUid")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(resultMap)
            .isNotNull()
            .containsEntry("uid", institutionMap.get("uid"));

        String updatedName = "Test Institution 2";
        String updatedBrandName = "Test Brand Name 2";
        String updatedActive = "N";

        // Update the institution.
        LinkedHashMap updatedMap = graphQlTester
            .document(String.format("""
                mutation {
                   updateInstitution(input: {
                        uid: "%s"
                        name: "%s"
                        brandName: "%s"
                        active: "%s"
                    }) {
                        uid
                        name
                        brandName
                        dealCount
                        memberCount
                        active
                    }
                }
                """, institutionMap.get("uid"), updatedName, updatedBrandName, updatedActive))
            .execute()
            .path("updateInstitution")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(updatedMap)
            .isNotNull()
            .containsEntry("uid", institutionMap.get("uid"))
            .containsEntry("name", updatedName)
            .containsEntry("brandName", updatedBrandName)
            .containsEntry("dealCount", 0)
            .containsEntry("memberCount", 1)
            .containsEntry("active", updatedActive);

        // Delete the institution.
        graphQlTester
            .document(String.format("""
                mutation {
                   deleteInstitution(institutionUid: "%s") {
                        uid
                        name
                        active
                    }
                }
                """, institutionMap.get("uid")))
            .execute()
            .path("deleteInstitution")
            .entity(Map.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                    .isNotNull()
                    .isNotEmpty()
                    .containsEntry("uid", institutionMap.get("uid"));
            });

        // Verify the institution is deleted.
        graphQlTester
            .document("""
                query getInstitutionByUid($uid: String!) {
                    getInstitutionByUid(uid: $uid) {
                        uid
                        name
                        brandName
                        active
                    }
                }
                """)
            .variable("uid", institutionMap.get("uid"))
            .execute().errors().satisfy(errors -> {
                assertThat(errors).hasSize(0);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenUnknownInstitution_whenDeletingInstitution_thenVerifyException() {

        graphQlTester
            .document(String.format("""
                mutation {
                   deleteInstitution(institutionUid: "%s") {
                        uid
                        name
                        active
                    }
                }
            """, TEST_DUMMY_UUID))
            .execute()
            .errors()
            .satisfy(errors -> {
                assertThat(errors).isNotEmpty();
                assertThat(errors.get(0).getMessage()).isEqualTo("Institution could not be deleted because it does not exist.");
            });

    }

}