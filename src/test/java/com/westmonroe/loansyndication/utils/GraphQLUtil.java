package com.westmonroe.loansyndication.utils;

import com.westmonroe.loansyndication.service.UserService;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphQLUtil {

    private GraphQLUtil() {
        throw new IllegalStateException("The class cannot be instantiated. It is a utility class.");
    }

    public static Map<String, Object> insertTestUser(HttpGraphQlTester graphQlTester, String institutionUid
            , String firstName, String lastName, String email, String password, String active) {
        return graphQlTester
                .document(String.format("""
                    mutation {
                        createUser(input: {
                            institution: {
                                uid: "%s"
                            }
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
                    """, institutionUid, firstName, lastName, email, password, active))
                .execute()
                .path("createUser")
                .entity(LinkedHashMap.class)
                .get();
    }

    public static Map<String, Object> insertTestInstitution(HttpGraphQlTester graphQlTester, String name, String brandName, String active) {
        return graphQlTester
                .document(String.format("""
                    mutation {
                        createInstitution(input: {
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
                    """, name, brandName, active))
                .execute()
                .path("createInstitution")
                .entity(LinkedHashMap.class)
                .get();
    }

    public static Map<String, Object> insertTestDeal(HttpGraphQlTester graphQlTester, String name, Integer dealIndustryId
            , String originatorUid, String initialLenderFlag, Integer initialLenderId, Integer dealStructureId
            , String dealType, String description, BigDecimal dealAmount, String borrowerDesc, String borrowerName
            , String borrowerCityName, String borrowerStateCode, String borrowerCountyName, Integer farmCreditEligId, String taxId
            , String borrowerIndustryCode, Integer businessAge, Integer defaultProbability , BigDecimal currYearEbita
            , String active) {
        return graphQlTester
                .document(String.format("""
                    mutation {
                       createDeal(input: {
                            name: "%s"
                            dealIndustry: {
                                id: %d
                            }
                            originator: {
                                uid: "%s"
                            }
                            initialLenderFlag: "%s"
                            initialLender: {
                                id: %d
                            }
                            dealStructure: {
                                id: %d
                            }
                            dealType: "%s"
                            description: "%s"
                            dealAmount: %.2f
                            borrowerDesc: "%s"
                            borrowerName: "%s"
                            borrowerCityName: "%s"
                            borrowerStateCode: "%s"
                            borrowerCountyName: "%s"
                            farmCreditElig: {
                                id: %d
                            }
                            taxId: "%s"
                            borrowerIndustry: {
                                code: "%s"
                            }
                            businessAge: %d
                            defaultProbability: %d
                            currYearEbita: %.2f
                            active: "%s"
                        }) {
                            uid
                            name
                            dealIndustry {
                                id
                                option
                            }
                            originator {
                                uid
                                name
                            }
                            initialLenderFlag
                            initialLender {
                                id
                                lenderName
                            }
                            relation
                            viewType
                            dealStructure {
                                id
                                option
                            }
                            dealType
                            description
                            dealAmount
                            borrowerDesc
                            borrowerName
                            borrowerCityName
                            borrowerStateCode
                            borrowerCountyName
                            farmCreditElig {
                                id
                                option
                            }
                            taxId
                            borrowerIndustry {
                                code
                                title
                            }
                            businessAge
                            defaultProbability
                            currYearEbita
                            createdBy {
                                uid
                                firstName
                                lastName
                            }
                            createdDate
                            updatedBy {
                                uid
                                firstName
                                lastName
                            }
                            updatedDate
                            active
                        }
                    }
                    """, name, dealIndustryId, originatorUid, initialLenderFlag, initialLenderId, dealStructureId
                   , dealType, description, dealAmount, borrowerDesc, borrowerName, borrowerCityName, borrowerStateCode
                   , borrowerCountyName, farmCreditEligId, taxId, borrowerIndustryCode, businessAge, defaultProbability
                   , currYearEbita, active))
                .execute()
                .path("createDeal")
                .entity(LinkedHashMap.class)
                .get();
    }

    public static Map<String, Object> insertTestEvent(HttpGraphQlTester graphQlTester, String dealUid, String name
            , Long eventTypeId, String projectedLaunchDate, String commitmentDate, String commentsDueByDate
            , String effectiveDate, String projectedCloseDate) {
        return graphQlTester
                .document(String.format("""
                    mutation {
                       createEvent(input: {
                            deal: {
                                uid: "%s"
                            }
                            name: "%s"
                            eventType: {
                                id: %d
                            }
                            projectedLaunchDate: "%s"
                            commitmentDate: "%s"
                            commentsDueByDate: "%s"
                            effectiveDate: "%s"
                            projectedCloseDate: "%s"
                        }) {
                            uid
                            deal {
                                uid
                                name
                            }
                            name
                            eventType {
                                id
                                name
                            }
                            stage {
                                id
                                name
                            }
                            projectedLaunchDate
                            commitmentDate
                            commentsDueByDate
                            effectiveDate
                            projectedCloseDate
                            createdBy {
                                uid
                                firstName
                                lastName
                            }
                            createdDate
                            updatedBy {
                                uid
                                firstName
                                lastName
                            }
                            updatedDate
                        }
                    }
                """, dealUid, name, eventTypeId, projectedLaunchDate, commitmentDate, commentsDueByDate, effectiveDate
                , projectedCloseDate))
                .execute()
                .path("createEvent")
                .entity(LinkedHashMap.class)
                .get();
    }

    public static Map<String, Object> insertTestDealMember(HttpGraphQlTester graphQlTester, String dealUid
            , String userUid) {
        return graphQlTester
                .document(String.format("""
                    mutation {
                       createDealMember(input: {
                            deal: {
                                uid: "%s"
                            }
                            user: {
                                uid: "%s"
                            }
                        }) {
                            deal {
                                uid
                                name
                            }
                            user {
                                uid
                                firstName
                                lastName
                            }
                            memberTypeCode
                            memberTypeDesc
                            createdBy {
                                uid
                                firstName
                                lastName
                            }
                            createdDate
                        }
                    }
                    """, dealUid, userUid))
                .execute()
                .path("createDealMember")
                .entity(LinkedHashMap.class)
                .get();
    }

    public static Map<String, Object> insertTestDealCovenant(HttpGraphQlTester graphQlTester, String dealUid, String entityName) {
        return graphQlTester
                .document(String.format("""
                    mutation {
                        createDealCovenant(input: {
                            deal: {
                                uid: "%s"
                            }
                            entityName: "%s"
                            categoryName: "Collateral"
                            covenantType: "Insurance"
                            frequency: "Quarterly"
                            nextEvalDate: "2021-02-02"
                            effectiveDate: "2021-01-01"
                        }) {
                            id
                            deal {
                                uid
                                name
                            }
                            entityName
                            categoryName
                            covenantType
                            frequency
                            nextEvalDate
                            effectiveDate
                            createdBy {
                                uid
                                firstName
                                lastName
                            }
                            createdDate
                            updatedBy {
                                uid
                                firstName
                                lastName
                            }
                            updatedDate
                         }
                    }
                    """, dealUid, entityName))
                .execute()
                .path("createDealCovenant")
                .entity(LinkedHashMap.class)
                .get();
    }

    public static Map<String, Object> insertTestDealFacility(HttpGraphQlTester graphQlTester, String dealUid
            , BigDecimal facilityAmount, Integer facilityTypeId, Integer tenor, Integer collateralId, String pricing
            , String creditSpreadAdj, Integer facilityPurposeId, String purposeDetail, Integer dayCountId, String guarInvFlag
            , String patronagePayingFlag, String farmCreditType, Integer revolverUtil, String upfrontFees, String unusedFees, String amortization
            , String maturityDate, String renewalDate, String lgdOption, Integer regulatoryLoanTypeId) {

        // conditionally pass renewalDate: null or renewalDate: "%s" into graphQL mutation
        String renewalDatePart = renewalDate == null ? "renewalDate: null" : String.format("renewalDate: \"%s\"", renewalDate);


        return graphQlTester
                .document(String.format("""
                    mutation {
                        createDealFacility(input: {
                            deal: {
                                uid: "%s"
                            }
                            facilityAmount: %.2f
                            facilityType: {
                                id: %d
                            }
                            tenor: %d
                            collateral: {
                                id: %d
                            }
                            pricing: "%s"
                            creditSpreadAdj: "%s"
                            facilityPurpose: {
                                id: %d
                            }
                            purposeDetail: "%s"
                            dayCount: {
                                id: %d
                            }
                            guarInvFlag: "%s"
                            patronagePayingFlag: "%s"
                            farmCreditType: "%s"
                            revolverUtil: %d
                            upfrontFees: "%s"
                            unusedFees: "%s"
                            amortization: "%s"
                            maturityDate: "%s"
                            %s
                            lgdOption: "%s"
                            regulatoryLoanType: {
                                id: %d
                            }
                        }) {
                            id
                            deal {
                                uid
                                name
                            }
                            facilityName
                            facilityAmount
                            facilityType {
                                id
                                option
                            }
                            tenor
                            pricing
                            creditSpreadAdj
                            facilityPurpose {
                                id
                                option
                            }
                            purposeDetail
                            collateral {
                                id
                                option
                            }
                            dayCount {
                                id
                                option
                            }
                            regulatoryLoanType {
                                id
                                option
                            }
                            guarInvFlag
                            patronagePayingFlag
                            farmCreditType
                            revolverUtil
                            upfrontFees
                            unusedFees
                            amortization
                            createdBy {
                                uid
                                firstName
                                lastName
                            }
                            createdDate
                            updatedBy {
                                uid
                                firstName
                                lastName
                            }
                            updatedDate
                            maturityDate
                            renewalDate
                            lgdOption
                         }
                    }
                    """, dealUid, facilityAmount, facilityTypeId, tenor, collateralId, pricing, creditSpreadAdj, facilityPurposeId
                       , purposeDetail, dayCountId, guarInvFlag, patronagePayingFlag, farmCreditType, revolverUtil, upfrontFees, unusedFees
                       , amortization, maturityDate, renewalDatePart, lgdOption, regulatoryLoanTypeId))
                .execute()
                .path("createDealFacility")
                .entity(LinkedHashMap.class)
                .get();
    }

    public static Map<String, Object> insertTestEventOriginationParticipant(HttpGraphQlTester graphQlTester, String eventUid
            , String institutionUid, String inviteRecipientUid, String message, String response) {
        return graphQlTester
                .document(String.format("""
                    mutation {
                       createEventOriginationParticipant(input: {
                            event: {
                                uid: "%s"
                            }
                            participant: {
                                uid: "%s"
                            }
                            inviteRecipient: {
                                uid: "%s"
                            }
                            message: "%s"
                            response: "%s"
                        }) {
                            id
                            event {
                                uid
                                name
                                stage {
                                  order
                                  name
                                  id
                                }
                                deal {
                                    originator {
                                        uid
                                        name
                                    }
                                }
                            }
                            participant {
                                uid
                                name
                            }
                            inviteRecipient {
                                uid
                                firstName
                                lastName
                            }
                            message
                            response
                            step {
                                id
                                name
                            }
                            createdBy {
                                uid
                                firstName
                                lastName
                            }
                            createdDate
                            updatedBy {
                                uid
                                firstName
                                lastName
                            }
                            updatedDate
                        }
                    }
                    """, eventUid, institutionUid, inviteRecipientUid, message, response))
                .execute()
                .path("createEventOriginationParticipant")
                .entity(LinkedHashMap.class)
                .get();
    }

    public static Authentication getJwtAuthentication(String username, UserService userService) {

        Jwt jwt = Jwt.withTokenValue("token")
                        .header("alg", "RS256")
                        .claims(claims -> {
                            claims.put("sub", username);
                            claims.put("email", username);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })
                        .build();
        UserDetails principal = userService.loadUserByUsername(username);
        return new JwtAuthenticationToken(jwt, principal.getAuthorities());
    }

}