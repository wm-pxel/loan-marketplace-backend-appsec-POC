package com.westmonroe.loansyndication.utils;

import com.westmonroe.loansyndication.model.*;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealCovenant;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventType;
import com.westmonroe.loansyndication.model.integration.UserDto;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;

public class ModelUtil {

    private ModelUtil() {
        throw new IllegalStateException("The class cannot be instantiated. It is a utility class.");
    }

    public static Deal createTestDeal(Integer dealNumber) {

        Deal deal = new Deal();
        deal.setName("Test Deal " + dealNumber);
        deal.setDealIndustry(new PicklistItem(15L, null, "Farm Credit", 99));
        deal.setInitialLenderFlag("N");
        deal.setDealStructure(new PicklistItem(4L, new PicklistCategory(1L, null), null, null));
        deal.setDealType("New");
        deal.setDescription("This is deal description " + dealNumber);
        deal.setDealAmount(BigDecimal.valueOf(10000000.00));
        deal.setBorrowerDesc("Test Borrower Desc " + dealNumber);
        deal.setBorrowerName("Test Borrower Name " + dealNumber);
        deal.setBorrowerCityName("Test Borrower City " + dealNumber);
        deal.setBorrowerStateCode("IL");
        deal.setBorrowerCountyName("Test Borrower County " + dealNumber);
        deal.setFarmCreditElig(new PicklistItem(1L, new PicklistCategory(1L, null), null, null));
        deal.setTaxId("99-9999999");
        deal.setBorrowerIndustry(new NaicsCode("238210", "Electrical Contractors and Other Wiring Installation Contractors"));
        deal.setBusinessAge(9);
        deal.setDefaultProbability(11);
        deal.setCurrYearEbita(BigDecimal.valueOf(12000000.22));
        deal.setActive("N");

        return deal;
    }

    public static Event createTestEvent(Deal deal, String name, EventType eventType, Stage stage) {

        Event event = new Event();
        event.setDeal(deal);
        event.setName(name);
        event.setEventType(eventType);
        event.setStage(stage);
        event.setCommitmentDate(LocalDate.now());
        event.setProjectedCloseDate(LocalDate.now());
        event.setEffectiveDate(LocalDate.now());

        return event;
    }

    public static Institution createTestInstitution(Integer institutionNumber) {

        Institution institution = new Institution();
        institution.setName("Test Institution Name " + institutionNumber);
        institution.setBrandName("Test Brand " + institutionNumber);
        institution.setOwner("Test Owner " + institutionNumber);
        institution.setActive("N");

        return institution;
    }

    public static InitialLender createTestInitialLender(Long initialLenderId, String lenderName, String active) {

        InitialLender initialLender = new InitialLender();
        initialLender.setId(initialLenderId);
        initialLender.setLenderName(lenderName);
        initialLender.setActive(active);

        return initialLender;
    }

    public static Stage createTestDealStage() {

        Stage stage = new Stage();
        stage.setId(1L);
        stage.setName("Test Deal Stage");
        stage.setOrder(1);

        return stage;
    }

    public static User createTestUser(Integer userNumber) {

        User user = new User();
        user.setFirstName("Test First " + userNumber);
        user.setLastName("Test Last " + userNumber);
        user.setEmail("test.user" + userNumber + "@westmonroe.com");
        user.setPassword("jhc9238dh9283dhd9283ncnSHY98129809_" + userNumber);
        user.setActive("N");

        return user;
    }

    public static UserDto createTestUserDto(String firstName, String lastName, String email, String active) {

        UserDto userDto = new UserDto();
        userDto.setFirstName(firstName);
        userDto.setLastName(lastName);
        userDto.setEmail(email);
        userDto.setActive(active);

        return userDto;
    }

    public static Role createTestRole(Integer roleNumber, String code, String name, String description) {

        Role role = new Role();
        role.setCode(( code == null ) ? "ROLE_" + roleNumber : code);
        role.setName(( name == null ) ? "Role Name " + roleNumber : name);
        role.setDescription(( description == null ) ? "Role Description " + roleNumber : description);

        return role;
    }

    public static DealDocument createTestDealDocument(Deal deal, String displayName, String documentName, String description) {

        DealDocument document = new DealDocument();
        document.setDeal(deal);
        document.setDisplayName(displayName);
        document.setDocumentName(documentName);
        document.setCategory(new DocumentCategory(1L, "", null, null));
        document.setDocumentType(MediaType.APPLICATION_PDF_VALUE);
        document.setDescription(description);
        document.setSource(SYSTEM_MARKETPLACE);

        return document;
    }

    public static DealCovenant createTestDealCovenant(Deal deal, String entityName) {

        DealCovenant covenant = new DealCovenant();
        covenant.setDeal(deal);
        covenant.setEntityName(entityName);
        covenant.setCategoryName("Collateral");
        covenant.setCovenantType("Insurance");
        covenant.setFrequency("Quarterly");
        covenant.setNextEvalDate(LocalDate.now().plus(30, ChronoUnit.DAYS));
        covenant.setEffectiveDate(LocalDate.now().plus(10, ChronoUnit.DAYS));

        return covenant;
    }

    public static DealFacility createTestDealFacility(Deal deal, BigDecimal facilityAmount, String facilityType
                    , Integer tenor, String pricing, String creditSpreadAdj, String purposeName, String purposeDetail
                    , Integer dayCount, String guarInvFlag, String patronagePayingFlag, String farmCreditType, String upfrontFees
                    , String unusedFees, String amortization, String lgdOption, String regulatoryLoanType) {

        DealFacility facility = new DealFacility();
        facility.setDeal(deal);
        facility.setFacilityAmount(facilityAmount);
        facility.setFacilityType(new PicklistItem(6L));
        facility.setTenor(tenor);
        facility.setPricing(pricing);
        facility.setCreditSpreadAdj(creditSpreadAdj);
        facility.setFacilityPurpose(new PicklistItem(10L));
        facility.setPurposeDetail(purposeDetail);
        facility.setDayCount(new PicklistItem(18L));
        facility.setRegulatoryLoanType(new PicklistItem(33L));
        facility.setGuarInvFlag(guarInvFlag);
        facility.setPatronagePayingFlag(patronagePayingFlag);
        facility.setFarmCreditType(farmCreditType);
        facility.setRevolverUtil(50);
        facility.setUpfrontFees(upfrontFees);
        facility.setUnusedFees(unusedFees);
        facility.setAmortization(amortization);
        facility.setLgdOption(lgdOption);

        return facility;
    }

}