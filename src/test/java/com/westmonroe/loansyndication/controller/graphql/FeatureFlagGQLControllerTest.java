package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.security.WithMockJwtUser;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.TestConstants.TEST_USER_EMAIL_1;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureHttpGraphQlTester
@Testcontainers
public class FeatureFlagGQLControllerTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1 )
    void givenNewFeatureFlag_whenPerformingCrudOperations_thenVerify() {
        String featureName = "TEST_FEATURE";
        String description = "TEST_DESC";
        String isEnabled = "Y"; // Since isEnabled is a String in your schema

        // **Create FeatureFlag**
        Map<String, Object> createResponse = graphQlTester
                .document("""
                    mutation($input: FeatureFlagInput!){
                        createFeatureFlag(input: $input) {
                            id
                            featureName
                            description
                            isEnabled
                        }
                    }           
                """)
                .variable("input", Map.of("featureName", featureName, "description", description, "isEnabled", isEnabled))
                .execute()
                .path("createFeatureFlag")
                .entity(Map.class)
                .get();

        assertThat(createResponse)
                .isNotNull()
                .containsEntry("featureName", featureName)
                .containsEntry("description", description)
                .containsEntry("isEnabled", isEnabled);

        Integer featureFlagId = (Integer) createResponse.get("id");

        // **Get FeatureFlag by ID**
        Map<String, Object> getByIdResponse = graphQlTester
                .document("""
                query($id: Int) {
                    getFeatureFlagById(id: $id) {
                        id
                        featureName
                        isEnabled
                    }
                }
                """)
                .variable("id", featureFlagId)
                .execute()
                .path("getFeatureFlagById")
                .entity(Map.class)
                .get();

        assertThat(getByIdResponse)
                .isNotNull()
                .containsEntry("id", featureFlagId)
                .containsEntry("featureName", featureName)
                .containsEntry("isEnabled", isEnabled);

        // **Get FeatureFlag by Feature Name**
        Map<String, Object> getByNameResponse = graphQlTester
                .document("""
                query($featureName: String) {
                    getFeatureFlagByFeatureName(featureName: $featureName) {
                        id
                        featureName
                        isEnabled
                    }
                }
                """)
                .variable("featureName", featureName)
                .execute()
                .path("getFeatureFlagByFeatureName")
                .entity(Map.class)
                .get();

        assertThat(getByNameResponse)
                .isNotNull()
                .containsEntry("id", featureFlagId)
                .containsEntry("featureName", featureName)
                .containsEntry("isEnabled", isEnabled);

        // **Update FeatureFlag**
        String updatedFeatureName = "UPDATED_FEATURE";
        String updatedIsEnabled = "N";

        Map<String, Object> updateResponse = graphQlTester
                .document("""
                mutation($input: FeatureFlagInput!) {
                    updateFeatureFlag(input: $input) {
                        id
                        featureName
                        isEnabled
                    }
                }
                """)
                .variable("input", Map.of(
                        "id", featureFlagId,
                        "featureName", updatedFeatureName,
                        "isEnabled", updatedIsEnabled
                ))
                .execute()
                .path("updateFeatureFlag")
                .entity(Map.class)
                .get();

        assertThat(updateResponse)
                .isNotNull()
                .containsEntry("id", featureFlagId)
                .containsEntry("featureName", updatedFeatureName)
                .containsEntry("isEnabled", updatedIsEnabled);

        // **Delete FeatureFlag**
        Map<String, Object> deleteResponse = graphQlTester
                .document("""
                mutation($id: Int) {
                    deleteFeatureFlag(id: $id) {
                        id
                        featureName
                    }
                }
                """)
                .variable("id", featureFlagId)
                .execute()
                .path("deleteFeatureFlag")
                .entity(Map.class)
                .get();

        assertThat(deleteResponse)
                .isNotNull()
                .containsEntry("id", featureFlagId)
                .containsEntry("featureName", updatedFeatureName);

        // **Verify Deletion**
        graphQlTester
                .document("""
                query($id: Int) {
                    getFeatureFlagById(id: $id) {
                        id
                        featureName
                    }
                }
                """)
                .variable("id", featureFlagId)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getMessage()).contains("Feature flag was not found for id.");
                });

        // **Delete All FeatureFlags**
        // First, create another FeatureFlag for testing
        Map<String, Object> anotherFeatureFlag = graphQlTester
                .document("""
                mutation($input: FeatureFlagInput!) {
                    createFeatureFlag(input: $input) {
                        id
                        featureName
                        description
                        isEnabled
                    }
                }
                """)
                .variable("input", Map.of(
                        "featureName", "ANOTHER_FEATURE",
                        "description", "FF_DESCRIPTION",
                        "isEnabled", "Y"
                ))
                .execute()
                .path("createFeatureFlag")
                .entity(Map.class)
                .get();

        // Now, delete all FeatureFlags
        List<Map> deleteAllResponse = graphQlTester
                .document("""
                mutation {
                    deleteAllFeatureFlags {
                        id
                        featureName
                    }
                }
                """)
                .execute()
                .path("deleteAllFeatureFlags")
                .entityList(Map.class)
                .get();

        assertThat(deleteAllResponse)
                .isNotNull()
                .isNotEmpty()
                .anySatisfy(featureFlag -> {
                    assertThat(featureFlag).containsEntry("featureName", "ANOTHER_FEATURE");
                });

    }
}
