package com.westmonroe.loansyndication.model.deal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.*;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import com.westmonroe.loansyndication.validation.ValidNaicsCode;
import com.westmonroe.loansyndication.validation.ValidPicklistItem;
import com.westmonroe.loansyndication.validation.ValidState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import static com.westmonroe.loansyndication.utils.Constants.*;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.*;
import static com.westmonroe.loansyndication.utils.DealStageEnum.STAGE_3;
import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.STEP_4;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deal", description = "Model for a Lamina deal.")
public class DealEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public DealEvent(Long id) {
        this.id = id;
    }

    public DealEvent(String uid) {
        this.uid = uid;
    }

    public DealEvent(Long id, String uid) {
        this.id = id;
        this.uid = uid;
    }

    @JsonIgnore
    private Long id;

    @Size(max = 36, message = "Unique ID cannot be greater than 36 characters.")
    private String uid;

    @Size(max = 55, message = "Deal External ID cannot be greater than 55 characters.")
    private String dealExternalId;

    @Size(min=1, max=80, message = "Name cannot be empty.")
    private String name;

    @ValidPicklistItem(category = "Deal Industry")
    private PicklistItem dealIndustry;

    @NotNull(message = "Originator cannot be null.")
    private Institution originator;

    @Pattern(regexp = REGEX_YN, message = "The initial lender flag must be Y or N.")
    private String initialLenderFlag;

    @NotNull(message = "Initial Lender cannot be null.")
    private InitialLender initialLender;

    private String openEventUid;

    /**
     * The following block is for calculating computed properties.
     */
    private String memberFlag;
    private String memberTypeCode;
    private String origInstUserFlag;
    private String partInstUserFlag;
    private String userRolesDesc;

    /**
     * The following block is for computed relationship and view type properties.
     */
    private String relation;
    private String viewType;

    @ValidPicklistItem(category = "Deal Structure")
    private PicklistItem dealStructure;

    @NotEmpty(message = "Deal type cannot be empty or null.")
    @Pattern(regexp = REGEX_DEAL_TYPE, message = "Deal type must be New, Renewal or Modification.")
    private String dealType;

    @Size(max = 5000, message = "Deal description cannot exceed 5,000 characters.")
    private String description;

    @Digits(integer=12, fraction=2, message = "Total transaction amount must be in valid numeric format.")
    private BigDecimal dealAmount;

    @ReadOnlyProperty
    private Integer lastFacilityNumber;

    @Size(max = 55, message = "Applicant External ID cannot be greater than 55 characters.")
    private String applicantExternalId;

    @Size(max = 5000, message = "Borrower description cannot exceed 5,000 characters.")
    private String borrowerDesc;

    @NotNull(message = "Borrower Name cannot be empty.")
    @Size(max=250, message = "Borrower Name cannot be exceed 250 characters.")
    private String borrowerName;

    @Size(max=50, message = "Borrower City Name cannot exceed 50 characters.")
    private String borrowerCityName;

    @ValidState(message = "Borrower State Code must be a valid state code.")
    private String borrowerStateCode;

    @Size(max=50, message = "Borrower County Name cannot exceed 50 characters.")
    private String borrowerCountyName;

    @ValidPicklistItem(category = "Farm Credit Eligibility")
    private PicklistItem farmCreditElig;

    @NotEmpty(message = "Tax id cannot be null or empty.")
    @Size(min=1, max=15, message = "Tax id cannot exceed 15 characters.")
    private String taxId;

    @ValidNaicsCode(message = "The borrower industry must be a valid NAICS code.")
    private NaicsCode borrowerIndustry;

    @PositiveOrZero(message = "Age of business must be a positive number.")
    private Integer businessAge;

    private Integer defaultProbability;

    @Digits(integer=12, fraction=2, message = "Curr Year Ebita must be valid decimal number.")
    private BigDecimal currYearEbita;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

    @Pattern(regexp = REGEX_YN, message = "Active must be Y or N.")
    private String active;

    private Event event;

    private EventParticipant eventParticipant;

    public String getRelation() {

        if ( "Y".equals(origInstUserFlag) ) {
            relation = ORIGINATOR.getDescription();
        } else if ( "Y".equals(partInstUserFlag) ) {
            relation = PARTICIPANT.getDescription();
        } else {
            relation = UNKNOWN.getDescription();
        }

        return relation;
    }

    public String getViewType() {

        if ( "Y".equals(origInstUserFlag) ) {

            if ( "O".equals(memberTypeCode) || userRolesDesc.contains("ACCESS_ALL_INST_DEALS") ) {
                viewType = VIEW_TYPE_FULL;
            } else {
                viewType = VIEW_TYPE_NO_ACCESS;
            }

        } else if ( "Y".equals(partInstUserFlag) ) {

            if ( "P".equals(memberTypeCode) || userRolesDesc.contains("ACCESS_ALL_INST_DEALS") ) {

                Long stageOrder = event == null ? 0L : event.getStage().getOrder();
                Long partStepOrder = eventParticipant == null ? 0L : eventParticipant.getStep().getOrder();

                if ( stageOrder >= STAGE_3.getOrder() && partStepOrder >= STEP_4.getOrder() ) {
                    viewType = VIEW_TYPE_FULL;
                } else {
                    viewType = VIEW_TYPE_SUMMARY;
                }

            } else {
                viewType = VIEW_TYPE_NO_ACCESS;
            }

        } else {
            viewType = VIEW_TYPE_NO_ACCESS;
        }

        return viewType;
    }

    // Temporary method to convert the DealEvent to Deal for legacy code.
    public Deal toDeal() {
        Deal deal = new Deal();

        deal.setId(this.id);
        deal.setUid(this.uid);
        deal.setDealExternalId(this.dealExternalId);
        deal.setName(this.name);
        deal.setDealIndustry(this.dealIndustry);
        deal.setOriginator(this.originator);
        deal.setInitialLenderFlag(this.initialLenderFlag);
        deal.setInitialLender(this.initialLender);
        deal.setMemberFlag(this.memberFlag);
        deal.setMemberTypeCode(this.memberTypeCode);
        deal.setOrigInstUserFlag(this.origInstUserFlag);
        deal.setPartInstUserFlag(this.partInstUserFlag);
        deal.setUserRolesDesc(this.userRolesDesc);
        deal.setDealStructure(this.dealStructure);
        deal.setDealType(this.dealType);
        deal.setDescription(this.description);
        deal.setDealAmount(this.dealAmount);
        deal.setLastFacilityNumber(this.lastFacilityNumber);
        deal.setApplicantExternalId(this.applicantExternalId);
        deal.setBorrowerDesc(this.borrowerDesc);
        deal.setBorrowerName(this.borrowerName);
        deal.setBorrowerCityName(this.borrowerCityName);
        deal.setBorrowerStateCode(this.borrowerStateCode);
        deal.setBorrowerCountyName(this.borrowerCountyName);
        deal.setFarmCreditElig(this.farmCreditElig);
        deal.setTaxId(this.taxId);
        deal.setBorrowerIndustry(this.borrowerIndustry);
        deal.setBusinessAge(this.businessAge);
        deal.setDefaultProbability(this.defaultProbability);
        deal.setCurrYearEbita(this.currYearEbita);
        deal.setCreatedBy(this.createdBy);
        deal.setCreatedDate(this.createdDate);
        deal.setUpdatedBy(this.updatedBy);
        deal.setUpdatedDate(this.updatedDate);
        deal.setActive(this.active);

        return deal;
    }

}