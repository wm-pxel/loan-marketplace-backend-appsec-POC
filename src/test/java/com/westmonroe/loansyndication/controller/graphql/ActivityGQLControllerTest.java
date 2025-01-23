package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.model.activity.ActivityType;
import com.westmonroe.loansyndication.security.WithMockJwtUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.westmonroe.loansyndication.utils.TestConstants.TEST_USER_EMAIL_1;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@Testcontainers
class ActivityGQLControllerTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingActivityTypes_whenRetrievingAllActivityTypes_thenVerifySize() {

        /*
         *  This user is a SUPER_ADM and can access this endpoint.
         */
        graphQlTester
            .document("""
            query {
                allActivityTypes {
                    id
                    name
                    category {
                        id
                        name
                    }
                }
            }
            """)
            .execute()
            .path("allActivityTypes")
            .entityList(ActivityType.class)
            .satisfies(activityTypes -> {
                assertThat(activityTypes)
                    .isNotEmpty()
                    .hasSize(25);
            });

    }

}