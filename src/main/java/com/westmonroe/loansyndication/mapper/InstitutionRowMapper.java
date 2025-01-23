package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.BillingCode;
import com.westmonroe.loansyndication.model.Institution;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InstitutionRowMapper implements RowMapper<Institution> {

    @Override
    public Institution mapRow(ResultSet rs, int rowNum) throws SQLException {

        Institution institution = new Institution();
        institution.setId(rs.getLong("INSTITUTION_ID"));
        institution.setUid(rs.getString("INSTITUTION_UUID"));
        institution.setName(rs.getString("INSTITUTION_NAME"));
        institution.setBrandName(rs.getString("BRAND_NAME"));
        institution.setCommunityExtension(rs.getString("COMMUNITY_EXT_DESC"));
        institution.setCommunityName(rs.getString("COMMUNITY_NAME"));
        institution.setCommunityNetworkID(rs.getString("COMMUNITY_NETWORK_ID"));
        institution.setLookupKey(rs.getString("LOOKUP_KEY_DESC"));
        institution.setOwner(rs.getString("OWNER_NAME"));
        institution.setPermissionSet(rs.getString("PERMISSION_SET_DESC"));
        institution.setActive(rs.getString("ACTIVE_IND"));
        institution.setDealCount(rs.getLong("DEAL_COUNT"));
        institution.setMemberCount(rs.getLong("MEMBER_COUNT"));
        institution.setSsoFlag(rs.getString("SSO_IND"));

        BillingCode billingCode = new BillingCode();
        billingCode.setCode(rs.getString("BILLING_CD"));
        billingCode.setDescription(rs.getString("BILLING_DESC"));
        institution.setBillingCode(billingCode);

        return institution;
    }

}