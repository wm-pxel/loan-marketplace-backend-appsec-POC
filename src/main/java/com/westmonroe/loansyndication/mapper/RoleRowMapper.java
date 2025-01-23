package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.Role;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleRowMapper implements RowMapper<Role> {

    @Override
    public Role mapRow(ResultSet rs, int rowNum) throws SQLException {

        Role role = new Role();
        role.setId(rs.getLong("ROLE_ID"));
        role.setCode(rs.getString("ROLE_CD"));
        role.setName(rs.getString("ROLE_NAME"));
        role.setDescription(rs.getString("ROLE_DESC"));
        role.setVisible(rs.getString("VISIBLE_IND"));

        return role;
    }

}