package com.westmonroe.loansyndication.mapper.deal;

import com.westmonroe.loansyndication.model.*;
import com.westmonroe.loansyndication.model.deal.Deal;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DealRowMapper implements RowMapper<Deal> {

    @Override
    public Deal mapRow(ResultSet rs, int rowNum) throws SQLException {

        Deal deal = new Deal();
        deal.setId(rs.getLong("DEAL_ID"));
        deal.setUid(rs.getString("DEAL_UUID"));
        deal.setDealExternalId(rs.getString("DEAL_EXTERNAL_UUID"));
        deal.setName(rs.getString("DEAL_NAME"));

        PicklistItem dealIndustry = new PicklistItem();
        dealIndustry.setId(rs.getLong("DEAL_INDUSTRY_ID"));
        dealIndustry.setOption(rs.getString("DEAL_INDUSTRY_NAME"));
        deal.setDealIndustry(dealIndustry);

        /*
         *  The following block is for calculated properties.
         */
        deal.setMemberFlag(rs.getString("MEMBER_IND"));
        deal.setMemberTypeCode(rs.getString("MEMBER_TYPE_CD"));
        deal.setOrigInstUserFlag(rs.getString("ORIG_INST_USER_IND"));
        deal.setPartInstUserFlag(rs.getString("PART_INST_USER_IND"));
        deal.setUserRolesDesc(rs.getString("USER_ROLES_DESC"));

        deal.setInitialLenderFlag(rs.getString("INITIAL_LENDER_IND"));

        if ( rs.getObject("INITIAL_LENDER_ID") != null ) {

            InitialLender lender = new InitialLender();
            lender.setId(rs.getLong("INITIAL_LENDER_ID"));
            lender.setLenderName(rs.getString("LENDER_NAME"));
            deal.setInitialLender(lender);

        }

        PicklistItem dealStructure = new PicklistItem();
        dealStructure.setId(rs.getLong("DEAL_STRUCTURE_ID"));
        dealStructure.setOption(rs.getString("DEAL_STRUCTURE_DESC"));
        deal.setDealStructure(dealStructure);

        deal.setDealType(rs.getString("DEAL_TYPE_DESC"));
        deal.setDescription(rs.getString("DEAL_DESC"));
        deal.setDealAmount(rs.getBigDecimal("DEAL_AMT"));

        deal.setLastFacilityNumber(rs.getInt("LAST_FACILITY_NBR"));

        deal.setApplicantExternalId(rs.getString("APPLICANT_EXTERNAL_UUID"));
        deal.setBorrowerDesc(rs.getString("BORROWER_DESC"));
        deal.setBorrowerName(rs.getString("BORROWER_NAME"));
        deal.setBorrowerCityName(rs.getString("BORROWER_CITY_NAME"));
        deal.setBorrowerStateCode(rs.getString("BORROWER_STATE_CD"));
        deal.setBorrowerCountyName(rs.getString("BORROWER_COUNTY_NAME"));

        if ( rs.getObject("FARM_CR_ELIG_ID") != null ) {

            PicklistItem farmCreditElig = new PicklistItem();
            farmCreditElig.setId(rs.getLong("FARM_CR_ELIG_ID"));
            farmCreditElig.setOption(rs.getString("FARM_CR_ELIG_DESC"));
            deal.setFarmCreditElig(farmCreditElig);

        }

        deal.setTaxId(rs.getString("TAX_ID_NBR"));

        if ( rs.getObject("BORROWER_INDUSTRY_CD") != null ) {

            NaicsCode borrowerIndustry = new NaicsCode();
            borrowerIndustry.setCode(rs.getString("BORROWER_INDUSTRY_CD"));
            borrowerIndustry.setTitle(rs.getString("BORROWER_INDUSTRY_NAME"));
            deal.setBorrowerIndustry(borrowerIndustry);

        }

        if ( rs.getObject("BUSINESS_AGE_QTY") != null ) {
            deal.setBusinessAge(rs.getInt("BUSINESS_AGE_QTY"));
        }

        if ( rs.getObject("DEFAULT_PROB_PCT") != null ) {
            deal.setDefaultProbability(rs.getInt("DEFAULT_PROB_PCT"));
        }

        if ( rs.getObject("CY_EBITA_AMT") != null ) {
            deal.setCurrYearEbita(rs.getBigDecimal("CY_EBITA_AMT"));
        }

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        deal.setCreatedBy(createdBy);

        deal.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        deal.setUpdatedBy(updatedBy);

        deal.setUpdatedDate(rs.getString("UPDATED_DATE"));
        deal.setActive(rs.getString("ACTIVE_IND"));

        Institution institution = new Institution();
        institution.setId(rs.getLong("INSTITUTION_ID"));
        institution.setUid(rs.getString("INSTITUTION_UUID"));
        institution.setName(rs.getString("INSTITUTION_NAME"));
        deal.setOriginator(institution);

        return deal;
    }

}