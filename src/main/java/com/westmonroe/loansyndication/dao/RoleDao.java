package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.RoleRowMapper;
import com.westmonroe.loansyndication.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.RoleQueryDef.*;

@Repository
@Slf4j
public class RoleDao {

    private final JdbcTemplate jdbcTemplate;

    public RoleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Role> findAll() {
        String sql = SELECT_ROLE + " WHERE VISIBLE_IND = 'Y' ORDER BY ROLE_NAME";
        return jdbcTemplate.query(sql, new RoleRowMapper());
    }

    public Role findById(Long id) {
        String sql = SELECT_ROLE + " WHERE ROLE_ID = ?";
        Role role;

        try {
            role = jdbcTemplate.queryForObject(sql, new RoleRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Role was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Role was not found for id.");

        }

        return role;
    }

    public Role save(Role role) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_ROLE, new String[] { "role_id" });
            ps.setString(1, role.getCode());
            ps.setString(2, role.getName());
            ps.setString(3, role.getDescription());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            role.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Role.");
            throw new DatabaseException("Error retrieving unique id for Role.");

        }

        return role;
    }

    public void update(Role r) {
        jdbcTemplate.update(UPDATE_ROLE, r.getCode(), r.getName(), r.getDescription(), r.getId());
    }

    public void delete(Role r) {
        delete(r.getId());
    }

    public void delete(Long id) {
        jdbcTemplate.update(DELETE_ROLE, id);
    }

}