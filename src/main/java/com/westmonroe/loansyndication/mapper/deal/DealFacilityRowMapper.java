package com.westmonroe.loansyndication.mapper.deal;

import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DealFacilityRowMapper implements RowMapper<DealFacility> {

    @Override
    public DealFacility mapRow(ResultSet rs, int rowNum) throws SQLException {

        DealFacility facility = new DealFacility();

        Deal deal = new Deal();
        deal.setId(rs.getLong("DEAL_ID"));
        deal.setUid(rs.getString("DEAL_UUID"));
        deal.setName(rs.getString("DEAL_NAME"));
        facility.setDeal(deal);

        facility.setId(rs.getLong("DEAL_FACILITY_ID"));
        facility.setFacilityExternalId(rs.getString("FACILITY_EXTERNAL_UUID"));
        facility.setFacilityName(rs.getString("FACILITY_NAME"));
        facility.setFacilityAmount(rs.getBigDecimal("FACILITY_AMT"));

        if ( rs.getObject("FACILITY_TYPE_ID") != null ) {

            PicklistItem facilityType = new PicklistItem();
            facilityType.setId(rs.getLong("FACILITY_TYPE_ID"));
            facilityType.setOption(rs.getString("FACILITY_TYPE_NAME"));
            facility.setFacilityType(facilityType);

        }

        if ( rs.getObject("TENOR_YRS_QTY") != null ) {
            facility.setTenor(rs.getInt("TENOR_YRS_QTY"));
        }

        if ( rs.getObject("PRICING_GRID_ID") != null ) {

            DealDocument document = new DealDocument();
            document.setId(rs.getLong("PRICING_GRID_ID"));
            document.setDeal(deal);
            document.setDisplayName(rs.getString("PG_DISPLAY_NAME"));
            document.setDocumentName(rs.getString("PG_DOCUMENT_NAME"));
            document.setDocumentType(rs.getString("PG_DOCUMENT_TYPE"));
            document.setDescription(rs.getString("PG_DOCUMENT_DESC"));
            document.setSource(rs.getString("PG_SOURCE_CD"));
            facility.setPricingGrid(document);

        }

        facility.setPricing(rs.getString("PRICING_DESC"));
        facility.setCreditSpreadAdj(rs.getString("CSA_DESC"));
        
        if ( rs.getObject("COLLATERAL_ID") != null ) {

            PicklistItem collateral = new PicklistItem();
            collateral.setId(rs.getLong("COLLATERAL_ID"));
            collateral.setOption(rs.getString("COLLATERAL_NAME"));
            facility.setCollateral(collateral);
       
        } 

        if ( rs.getObject("FACILITY_PURPOSE_ID") != null ) {
            
            PicklistItem facilityPurpose = new PicklistItem();
            facilityPurpose.setId(rs.getLong("FACILITY_PURPOSE_ID"));
            facilityPurpose.setOption(rs.getString("FACILITY_PURPOSE_NAME"));
            facility.setFacilityPurpose(facilityPurpose);
        }


        facility.setPurposeDetail(rs.getString("PURPOSE_TEXT"));

        if ( rs.getObject("DAY_COUNT_ID") != null ) {

            PicklistItem dayCount = new PicklistItem();
            dayCount.setId(rs.getLong("DAY_COUNT_ID"));
            dayCount.setOption(rs.getString("DAY_COUNT_NAME"));
            facility.setDayCount(dayCount);

        }

        if ( rs.getObject("REGULATORY_LOAN_TYPE_ID") != null ) {

            PicklistItem regulatoryLoanType = new PicklistItem();
            regulatoryLoanType.setId(rs.getLong("REGULATORY_LOAN_TYPE_ID"));
            regulatoryLoanType.setOption(rs.getString("REGULATORY_LOAN_TYPE_NAME"));
            facility.setRegulatoryLoanType(regulatoryLoanType);

        }

        facility.setGuarInvFlag(rs.getString("GUAR_INV_IND"));
        facility.setPatronagePayingFlag(rs.getString("PATRONAGE_PAYING_IND"));
        facility.setFarmCreditType(rs.getString("FARM_CREDIT_TYPE_NAME"));

        if ( rs.getObject("REV_UTIL_PCT") != null ) {
            facility.setRevolverUtil(rs.getInt("REV_UTIL_PCT"));
        }

        facility.setUpfrontFees(rs.getString("UPFRONT_FEES_DESC"));
        facility.setUnusedFees(rs.getString("UNUSED_FEES_DESC"));
        facility.setAmortization(rs.getString("AMORTIZATION_DESC"));
        facility.setLgdOption(rs.getString("LGD_OPTION"));

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        facility.setCreatedBy(createdBy);

        facility.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        facility.setUpdatedBy(updatedBy);

        facility.setMaturityDate(rs.getObject("MATURITY_DATE", LocalDate.class));
        facility.setRenewalDate(rs.getObject("RENEWAL_DATE", LocalDate.class));

        facility.setUpdatedDate(rs.getString("UPDATED_DATE"));


        return facility;
    }

}