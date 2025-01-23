package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.model.EndUserAgreement;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.security.WithMockJwtUser;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.GraphQLUtil.insertTestInstitution;
import static com.westmonroe.loansyndication.utils.GraphQLUtil.insertTestUser;
import static com.westmonroe.loansyndication.utils.RoleDefEnum.RECV_ALL_INST_INVS;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureHttpGraphQlTester
@Testcontainers
class UserGQLControllerTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingUsers_whenRetrievingCurrentUser_thenVerify() {

        graphQlTester
            .document("""
                query {
                    getCurrentUser {
                        uid
                        institution {
                            uid
                            name
                        }
                        firstName
                        lastName
                        email
                        roles {
                            id
                            code
                            name
                        }
                        active
                    }
                }
            """)
            .execute()
            .path("getCurrentUser")
            .entity(Map.class)
            .satisfies(userMap -> {
                assertThat(userMap)
                    .isNotNull()
                    .containsEntry("uid", "4d7ac607-9c66-41bc-bf6c-1458d192ff75")
                    .containsEntry("firstName", "Lenor")
                    .containsEntry("lastName", "Anderson")
                    .containsEntry("email", "Lenor.Anderson@test.com");
        });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingUser_whenAddingAndDeletingRoles_thenVerify() {

        // Use "Annie Palinto" for this test, so get her current roles.
        User user = graphQlTester
            .document("""
                query getUserByUid($uid: String!) {
                    getUserByUid(uid: $uid) {
                        uid
                        firstName
                        lastName
                        roles {
                            id
                            code
                            name
                        }
                    }
                }
            """)
            .variable("uid", TEST_USER_UUID_2)
            .execute()
            .path("getUserByUid")
            .entity(User.class)
            .get();

        // Verify that Annie Palinto does not have the NDA_MGR role.
        assertThat(user.getRoles())
            .hasSize(5)
            .extracting(Role::getCode)
                .doesNotContain("NDA_MGR")
                .contains("MNG_PART_INST");

        // Add the NDA_MGR role (roleId = 5) to Annie Palinto.
        graphQlTester
            .document(String.format("""
                mutation {
                   addRoleToUser(userUid: "%s", roleId: %d) {
                        uid
                        firstName
                        lastName
                        roles {
                            id
                            code
                            name
                        }
                    }
                }
            """, TEST_USER_UUID_2, 5))
            .execute()
            .path("addRoleToUser")
            .entity(User.class)
            .satisfies(userResult -> {
                assertThat(userResult.getRoles())
                    .isNotNull()
                    .hasSize(6)
                    .extracting(Role::getCode)
                        .contains("NDA_MGR")
                        .contains("MNG_PART_INST");
            });

        // Delete the MNG_PART_INST role (roleId = 8) to Annie Palinto.
        graphQlTester
            .document(String.format("""
                mutation {
                   deleteRoleFromUser(userUid: "%s", roleId: %d) {
                        uid
                        firstName
                        lastName
                        roles {
                            id
                            code
                            name
                        }
                    }
                }
            """, TEST_USER_UUID_2, 8))
            .execute()
            .path("deleteRoleFromUser")
            .entity(User.class)
            .satisfies(userResult -> {
                assertThat(userResult.getRoles())
                    .isNotNull()
                    .hasSize(5)
                    .extracting(Role::getCode)
                        .contains("NDA_MGR")
                        .doesNotContain("MNG_PART_INST");
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewUser_whenPerformingCrudOperations_thenVerify() {

        // Perform the GraphQL mutation for inserting an institution.
        Map<String, Object> institutionMap = insertTestInstitution(graphQlTester, "ABC Credit Association"
                , "Any Brand", "Y");

        String firstName = "Wilma";
        String lastName = "Flintstone";
        String email = "wilma.flintstone@westmonroe.com";
        String password = "password1234";
        String active = "Y";

        // Perform the GraphQL mutation for inserting a user.
        Map<String, Object> userMap = insertTestUser(graphQlTester, (String) institutionMap.get("uid"), firstName, lastName, email, password, active);

        assertThat(userMap).isNotNull();
        assertThat(userMap.get("uid")).isNotNull();
        assertThat(((Map) userMap.get("institution"))).containsEntry("uid", institutionMap.get("uid"));
        assertThat(userMap)
            .containsEntry("firstName", firstName)
            .containsEntry("lastName", lastName)
            .containsEntry("email", email)
            .containsEntry("active", active);

        // Get the user by its UID.
        LinkedHashMap resultMap = graphQlTester
            .document("""
                query getUserByUid($uid: String!) {
                    getUserByUid(uid: $uid) {
                        uid
                        institution {
                            uid
                            name
                        }
                        firstName
                        lastName
                        email
                        active
                    }
                }
            """)
            .variable("uid", userMap.get("uid"))
            .execute()
            .path("getUserByUid")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(resultMap).isNotNull();
        assertThat(resultMap).containsEntry("uid", userMap.get("uid"));
        assertThat(((Map) resultMap.get("institution"))).containsEntry("uid", institutionMap.get("uid"));
        assertThat(resultMap)
            .containsEntry("firstName", firstName)
            .containsEntry("lastName", lastName)
            .containsEntry("email", email)
            .containsEntry("active", active);

        String updatedFirstName = "Will";
        String updatedLastName = "Flipstone";
        String updatedEmail = "will.flintstone@westmonroe.com";
        String updatedPassword = "password123456";
        String updatedActive = "N";

        // Update the user.
        LinkedHashMap updatedMap = graphQlTester
            .document(String.format("""
                mutation {
                   updateUser(input: {
                        uid: "%s"
                        firstName: "%s"
                        lastName: "%s"
                        email: "%s"
                        password: "%s"
                        active: "%s"
                    }) {
                        uid
                        institution {
                            uid
                            name
                        }
                        firstName
                        lastName
                        email
                        active
                    }
                }
            """, userMap.get("uid"), updatedFirstName, updatedLastName, updatedEmail, updatedPassword, updatedActive))
            .execute()
            .path("updateUser")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(updatedMap)
            .isNotNull()
            .containsEntry("firstName", updatedFirstName)
            .containsEntry("lastName", updatedLastName)
            .containsEntry("email", updatedEmail)
            .containsEntry("active", updatedActive);

        // Verify Deleting User throws error
        graphQlTester
                .document(String.format("""
                mutation {
                   deleteUser(userUid: "%s") {
                        uid
                        firstName
                        lastName
                        active
                    }
                }
            """, userMap.get("uid")))
                .execute()
                .errors().satisfy(errors -> {
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getMessage()).isEqualTo("There must be at least one user that is a deal recipient for this institution");
                });

        firstName = "Fred";
        lastName = "Flintstone";
        email = "fred.flintstone@westmonroe.com";
        password = "password1234";
        active = "Y";

        // Perform the GraphQL mutation for inserting a user.
        Map<String, Object> secondUserMap = insertTestUser(graphQlTester, (String) institutionMap.get("uid"), firstName, lastName, email, password, active);

        // Add Deal Inv Recip for second useR
        graphQlTester
                .document(String.format("""
                mutation {
                   addRoleToUser(userUid: "%s", roleId: %d) {
                        uid
                        firstName
                        lastName
                        roles {
                            id
                            code
                            name
                        }
                    }
                }
            """, secondUserMap.get("uid"), RECV_ALL_INST_INVS.getId()))
            .execute()
            .path("addRoleToUser")
            .entity(User.class)
            .satisfies(userResult -> {
                assertThat(userResult.getRoles())
                        .isNotNull()
                        .hasSize(1)
                        .extracting(Role::getCode)
                        .contains("RECV_ALL_INST_INVS");
            });

        graphQlTester
            .document(String.format("""
                mutation {
                   deleteUser(userUid: "%s") {
                        uid
                        firstName
                        lastName
                        active
                    }
                }
            """, userMap.get("uid")))
            .execute()
            .path("deleteUser")
            .entity(Map.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                    .isNotNull()
                    .isNotEmpty()
                    .containsEntry("uid", userMap.get("uid"))
                    .containsEntry("firstName", updatedFirstName)
                    .containsEntry("lastName", updatedLastName)
                    .containsEntry("active", updatedActive);
            });

        // Verify the User is deleted.
        graphQlTester
            .document("""
                query getUserByUid($uid: String!) {
                    getUserByUid(uid: $uid) {
                        uid
                        institution {
                            uid
                            name
                        }
                        firstName
                        lastName
                        email
                        active
                    }
                }
            """)
            .variable("uid", userMap.get("uid"))
            .execute().errors().satisfy(errors -> {
                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getMessage()).isEqualTo("User was not found for uid.");
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewUser_whenCreatingNewUserInvite_thenVerifyInviteStatusIsInvited() {

        String firstName = "Barney";
        String lastName = "Rubble";
        String email = "barney.rubble@westmonroe.com";

        // Perform the GraphQL mutation for creating the user invite with some initial roles.
        User user =  graphQlTester
            .document(String.format("""
                mutation {
                    createInstitutionUserInvite(input: {
                        firstName: "%s"
                        lastName: "%s"
                        email: "%s"
                        roles: [ { id: 1 }, { id: 2 }, { id: 3 }, { id: 4 } ]
                    }) {
                        uid
                        firstName
                        lastName
                        institution {
                            uid
                            name
                        }
                        email
                        inviteStatus {
                            code
                            description
                        }
                        roles {
                            id
                            code
                            name
                        }
                        active
                    }
                }
            """, firstName, lastName, email))
            .execute()
            .path("createInstitutionUserInvite")
            .entity(User.class)
            .get();

        assertThat(user)
            .isNotNull()
            .hasFieldOrPropertyWithValue("firstName", firstName)
            .hasFieldOrPropertyWithValue("lastName", lastName)
            .hasFieldOrPropertyWithValue("active", "Y");
        assertThat(user.getInviteStatus().getCode()).isEqualTo("I");
        assertThat(user.getRoles())
            .isNotNull()
            .hasSize(3);

        // Get the user by its UID.
        User savedUser = graphQlTester
            .document("""
            query getUserByUid($uid: String!) {
                getUserByUid(uid: $uid) {
                    uid
                    institution {
                        uid
                        name
                    }
                    firstName
                    lastName
                    email
                    inviteStatus {
                        code
                        description
                    }
                    roles {
                        id
                        code
                        name
                    }
                    active
                }
            }
            """)
            .variable("uid", user.getUid())
            .execute()
            .path("getUserByUid")
            .entity(User.class)
            .get();

        assertThat(savedUser)
            .isNotNull()
            .hasFieldOrPropertyWithValue("firstName", firstName)
            .hasFieldOrPropertyWithValue("lastName", lastName)
            .hasFieldOrPropertyWithValue("active", "Y");
        assertThat(savedUser.getInviteStatus().getCode()).isEqualTo("I");
        assertThat(user.getRoles())
            .isNotNull()
            .hasSize(3);
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenUnknownUser_whenDeletingUser_thenVerifyException() {

        graphQlTester
            .document(String.format("""
            mutation {
               deleteUser(userUid: "%s") {
                    uid
                    firstName
                    lastName
                    active
                }
            }
        """, TEST_DUMMY_UUID))
        .execute()
        .errors()
        .satisfy(errors -> {
            assertThat(errors).isNotEmpty();
            assertThat(errors.get(0).getMessage()).isEqualTo("User could not be deleted because it does not exist.");
        });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenDealMembers_whenGettingAllAvailableUsers_thenVerify() {

        graphQlTester
            .document("""
                query getDealMemberUsersAvailableByDealUid($uid: String!) {
                    getDealMemberUsersAvailableByDealUid(uid: $uid) {
                        uid
                        institution {
                            uid
                            name
                        }
                        firstName
                        lastName
                        email
                        active
                    }
                }
            """)
            .variable("uid", TEST_DEAL_UUID_2)
            .execute()
            .path("getDealMemberUsersAvailableByDealUid")
            .entityList(User.class)
            .satisfies(users -> {
                assertThat(users)
                    .isNotEmpty()
                    .hasSize(2);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenDealMembers_whenGettingAllTeamMembers_thenVerify() {

        graphQlTester
            .document("""
                query getDealMemberUsersByDealUid($uid: String!) {
                    getDealMemberUsersByDealUid(uid: $uid) {
                        uid
                        institution {
                            uid
                            name
                        }
                        firstName
                        lastName
                        email
                        active
                    }
                }
            """)
            .variable("uid", TEST_DEAL_UUID_2)
            .execute()
            .path("getDealMemberUsersByDealUid")
            .entityList(User.class)
            .satisfies(users -> {
                assertThat(users)
                    .isNotEmpty()
                    .hasSize(2);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewUser_whenAcceptingEndUserAgreement_thenVerify() {
        EndUserAgreement eua  = graphQlTester
            .document(String.format("""
                query {
                    getEndUserAgreement {
                        id
                        content
                        createdDate
                        billingCode {
                            code
                        }
                    }
                }
            """))
            .execute()
            .path("getEndUserAgreement")
            .entity(EndUserAgreement.class)
            .get();

        assertThat(eua.getBillingCode().getCode()).isEqualTo("FREE");
        assertThat(eua.getContent()).isNotEmpty();
        Boolean result = graphQlTester
            .document(String.format("""
                mutation {
                    agreeToEndUserAgreement(euaId: %d)
                }
            """, eua.getId()))
            .execute()
            .path("agreeToEndUserAgreement")
            .entity(Boolean.class)
            .get();

        assertThat(result).isTrue();
    }

}