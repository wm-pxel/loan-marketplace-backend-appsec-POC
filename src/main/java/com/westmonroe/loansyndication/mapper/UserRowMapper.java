package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.InviteStatus;
import com.westmonroe.loansyndication.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {

        User user = new User();
        user.setId(rs.getLong("USER_ID"));
        user.setUid(rs.getString("USER_UUID"));
        user.setFirstName(rs.getString("FIRST_NAME"));
        user.setLastName(rs.getString("LAST_NAME"));
        user.setEmail(rs.getString("EMAIL_ADDR"));
        user.setPassword(rs.getString("PASSWORD_DESC"));
        user.setActive(rs.getString("ACTIVE_IND"));
        user.setSystemUser(rs.getString("SYSTEM_USER_IND"));
        user.setCreatedDate(rs.getString("CREATED_DATE"));

        // Add the invite status code, if the code is not null.
        if ( rs.getString("INVITE_STATUS_CD") != null ) {

            InviteStatus inviteStatus = new InviteStatus();
            inviteStatus.setCode(rs.getString("INVITE_STATUS_CD"));
            inviteStatus.setDescription(rs.getString("INVITE_STATUS_DESC"));
            user.setInviteStatus(inviteStatus);

        }

        // Add the institution as an object.
        Institution institution = new Institution();
        institution.setId(rs.getLong("INSTITUTION_ID"));
        institution.setUid(rs.getString("INSTITUTION_UUID"));
        institution.setName(rs.getString("INSTITUTION_NAME"));
        institution.setActive(rs.getString("INSTITUTION_ACTIVE_IND"));
        institution.setSsoFlag(rs.getString("SSO_IND"));
        user.setInstitution(institution);

        return user;
    }

}