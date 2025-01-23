package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.mapper.RoleRowMapper;
import com.westmonroe.loansyndication.mapper.UserRoleRowMapper;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.UserRoleQueryDef.*;

@Repository
@Slf4j
public class UserRoleDao {

    private final JdbcTemplate jdbcTemplate;

    public UserRoleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Role> findRolesByUserId(Long userId) {
        String sql = SELECT_USER_ROLES + " WHERE URX.USER_ID = ? ORDER BY ROLE_NAME";
        return jdbcTemplate.query(sql, new RoleRowMapper(), userId);
    }

    public List<Role> findRolesByUserUid(String userUid) {
        String sql = SELECT_USER_ROLES + " WHERE UI.USER_UUID = ? ORDER BY ROLE_NAME";
        return jdbcTemplate.query(sql, new RoleRowMapper(), userUid);
    }

    public List<UserRole> findRolesByUsers(List<User> users) {

        if  ( users == null || users.isEmpty() ) {
            return Collections.emptyList();
        }

        List<Long> userIds = users.stream().map(User::getId).toList();
        String sql = SELECT_USER_ROLES + " WHERE URX.USER_ID IN (" + String.join(",", Collections.nCopies(userIds.size(), "?")) + ")";

        return jdbcTemplate.query(sql, new UserRoleRowMapper(), userIds.toArray());
    }

    public void saveUserRole(User user, Role role) {

        try {
            jdbcTemplate.update(INSERT_USER_ROLE, user.getId(), role.getId());
        } catch ( DuplicateKeyException pke ) {

            log.error(String.format("User Role could not be saved because of DuplicateKeyException. ( user id = %d, role id = %d )"
                    , user.getId(), role.getId()));
            throw new DataIntegrityException("User Role could not be saved because it already exists.");

        }
    }

    public int deleteByUserAndRole(User user, Role role) {
        String sql = DELETE_USER_ROLE + " WHERE USER_ID = ? AND ROLE_ID = ?";
        return jdbcTemplate.update(sql, user.getId(), role.getId());
    }

    public int deleteByUserId(Long userId) {
        String sql = DELETE_USER_ROLE + " WHERE USER_ID = ?";
        return jdbcTemplate.update(sql, userId);
    }

    public int deleteByUserUid(String userUid) {
        String sql = DELETE_USER_ROLE + " WHERE USER_ID = ( SELECT USER_ID FROM USER_INFO WHERE USER_UUID = ? )";
        return jdbcTemplate.update(sql, userUid);
    }

    public int deleteAllByInstitutionId(Long institutionId) {
        String sql = DELETE_USER_ROLE + " WHERE USER_ID IN ( SELECT USER_ID FROM USER_INFO WHERE INSTITUTION_ID = ? )";
        return jdbcTemplate.update(sql, institutionId);
    }

}