package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.UserRole;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRoleRowMapper implements RowMapper<UserRole> {

    @Override
    public UserRole mapRow(ResultSet rs, int rowNum) throws SQLException {

        UserRole userRole = new UserRole();

        // Create the user object.
        User user = new User();
        user.setId(rs.getLong("USER_ID"));
        user.setUid(rs.getString("USER_UUID"));
        user.setFirstName(rs.getString("FIRST_NAME"));
        user.setLastName(rs.getString("LAST_NAME"));
        user.setEmail(rs.getString("EMAIL_ADDR"));
        user.setPassword(rs.getString("PASSWORD_DESC"));
        user.setActive(rs.getString("ACTIVE_IND"));
        user.setCreatedDate(rs.getString("CREATED_DATE"));

        // Add the institution as an object.
        Institution institution = new Institution();
        institution.setId(rs.getLong("INSTITUTION_ID"));
        institution.setUid(rs.getString("INSTITUTION_UUID"));
        institution.setName(rs.getString("INSTITUTION_NAME"));
        user.setInstitution(institution);

        // Add the user object to the user role object.
        userRole.setUser(user);

        // Create the role object.
        Role role = new Role();
        role.setId(rs.getLong("ROLE_ID"));
        role.setCode(rs.getString("ROLE_CD"));
        role.setDescription(rs.getString("ROLE_DESC"));
        role.setName(rs.getString("ROLE_NAME"));

        // Add the role object to the user role object.
        userRole.setRole(role);

        return userRole;
    }

}