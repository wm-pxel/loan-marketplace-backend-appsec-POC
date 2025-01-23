package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.UserRowMapper;
import com.westmonroe.loansyndication.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.UserQueryDef.*;

@Repository
@Slf4j
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> findAll() {
        String sql = SELECT_USER + " ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public List<User> findAllNonSystem() {
        String sql = SELECT_USER + " WHERE UI.SYSTEM_USER_IND = 'N' ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public List<User> findAllByInstitutionId(Long institutionId) {
        String sql = SELECT_USER + " WHERE UI.INSTITUTION_ID = ? AND UI.SYSTEM_USER_IND = 'N' ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new UserRowMapper(), institutionId);
    }

    public List<User> findAllByInstitutionUid(String institutionUid) {
        String sql = SELECT_USER + " WHERE II.INSTITUTION_UUID = ? ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new UserRowMapper(), institutionUid);
    }

    public List<User> findAllNonSystemByInstitutionUid(String institutionUid) {
        String sql = SELECT_USER + " WHERE II.INSTITUTION_UUID = ? AND UI.SYSTEM_USER_IND = 'N' ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new UserRowMapper(), institutionUid);
    }

    public List<User> findAllByInstitutionUidAndUserRoleId(String institutionUid, Long roleId) {
        // Concatenate the additional SQL conditions to the base query
        String sql = SELECT_USER + " LEFT JOIN USER_ROLE_XREF URX ON UI.USER_ID = URX.USER_ID "
                                    + "WHERE II.INSTITUTION_UUID = ? AND URX.ROLE_ID = ?";

        // Execute the query with the provided parameters
        return jdbcTemplate.query(sql, new UserRowMapper(), institutionUid, roleId);
    }

    public List<User> findAllNonSystemByInstitutionUidAndUserRoleId(String institutionUid, Long roleId) {
        String sql = SELECT_USER + " LEFT JOIN USER_ROLE_XREF URX ON UI.USER_ID = URX.USER_ID "
                + "WHERE II.INSTITUTION_UUID = ? AND URX.ROLE_ID = ? AND UI.SYSTEM_USER_IND = 'N'";

        return jdbcTemplate.query(sql, new UserRowMapper(), institutionUid, roleId);
    }

    public List<User> findAllDealMemberUsersAvailableByDealUidAndInstitutionId(String dealUid, Long institutionId) {

        String sql = SELECT_USER + " WHERE UI.INSTITUTION_ID = ? "
                                    + "AND UI.USER_ID NOT IN ( SELECT DM.USER_ID "
                                                              + "FROM DEAL_MEMBER DM LEFT JOIN DEAL_INFO DI "
                                                                + "ON DM.DEAL_ID = DI.DEAL_ID LEFT JOIN USER_INFO UI "
                                                                + "ON DM.USER_ID = UI.USER_ID "
                                                             + "WHERE DI.DEAL_UUID = ? "
                                                               + "AND UI.INSTITUTION_ID = ?) "
                                  + "ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new UserRowMapper(), institutionId, dealUid, institutionId);
    }

    public List<User> findAllNonSystemDealMemberUsersAvailableByDealUidAndInstitutionId(String dealUid, Long institutionId) {

        String sql = SELECT_USER + " WHERE UI.INSTITUTION_ID = ? "
                + "AND UI.USER_ID NOT IN ( SELECT DM.USER_ID "
                + "FROM DEAL_MEMBER DM LEFT JOIN DEAL_INFO DI "
                + "ON DM.DEAL_ID = DI.DEAL_ID LEFT JOIN USER_INFO UI "
                + "ON DM.USER_ID = UI.USER_ID "
                + "WHERE DI.DEAL_UUID = ? "
                + "AND UI.INSTITUTION_ID = ?) "
                + "AND UI.SYSTEM_USER_IND = 'N' "
                + "ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new UserRowMapper(), institutionId, dealUid, institutionId);
    }

    public List<User> findAllDealMemberUsers(String dealUid, Long institutionId) {

        String sql = SELECT_USER + " WHERE UI.USER_ID IN ( SELECT DM.USER_ID "
                                                          + "FROM DEAL_MEMBER DM LEFT JOIN DEAL_INFO DI "
                                                            + "ON DM.DEAL_ID = DI.DEAL_ID LEFT JOIN USER_INFO UI "
                                                            + "ON DM.USER_ID = UI.USER_ID "
                                                         + "WHERE DI.DEAL_UUID = ? "
                                                           + "AND UI.INSTITUTION_ID = ?) "
                                  + "ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new UserRowMapper(), dealUid, institutionId);
    }

    public List<User> findAllNonSystemDealMemberUsers(String dealUid, Long institutionId) {

        String sql = SELECT_USER + " WHERE UI.USER_ID IN ( SELECT DM.USER_ID "
                + "FROM DEAL_MEMBER DM LEFT JOIN DEAL_INFO DI "
                + "ON DM.DEAL_ID = DI.DEAL_ID LEFT JOIN USER_INFO UI "
                + "ON DM.USER_ID = UI.USER_ID "
                + "WHERE DI.DEAL_UUID = ? "
                + "AND UI.INSTITUTION_ID = ? "
                + "AND UI.SYSTEM_USER_IND = 'N') "
                + "ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new UserRowMapper(), dealUid, institutionId);
    }

    public Long findIdByInstitutionUidAndUserUid(String institutionUid, String userUid) {

        String sql = SELECT_USER_ID + " WHERE II.INSTITUTION_UUID = ? AND UI.USER_UUID = ?";
        Long userId;

        try {
            userId = jdbcTemplate.queryForObject(sql, Long.class, institutionUid, userUid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("User was not found for institution (  %s ) and user ( %s ).", institutionUid, userUid));
            throw new DataNotFoundException("User was not found for Institution.");

        }

        return userId;
    }

    public User findById(Long id) {
        String sql = SELECT_USER + " WHERE UI.USER_ID = ?";
        User user;

        try {
            user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("User was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("User was not found for id.");

        }

        return user;
    }

    public User findByUid(String uid) {

        String sql = SELECT_USER + " WHERE UI.USER_UUID = ?";
        User user;

        try {
            user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), uid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("User was not found for uid. ( uid = %s )", uid));
            throw new DataNotFoundException("User was not found for uid.");

        }

        return user;
    }

    public User findByEmail(String email) {

        String sql = SELECT_USER + " WHERE LOWER(UI.EMAIL_ADDR) = LOWER(?)";
        User user;

        try {
            user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("User was not found for email. ( email = %s )", email));
            throw new DataNotFoundException("User was not found for email.");

        }

        return user;
    }

    public User findByInstitutionUidAndUserUid(String institutionUid, String userUid) {

        String sql = SELECT_USER + " WHERE II.INSTITUTION_UUID = ? AND UI.USER_UUID = ?";
        User user;

        try {
            user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), institutionUid, userUid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("User was not found for institution ( %s ) and user ( %s ).", institutionUid, userUid));
            throw new DataNotFoundException("User was not found for Institution.");

        }

        return user;
    }

    public User save(User user) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(INSERT_USER, new String[] { "user_id" });
            ps.setString(1, user.getUid());
            ps.setLong(2, user.getInstitution().getId());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPassword());

            if ( user.getInviteStatus() == null || user.getInviteStatus().getCode() == null ) {
                ps.setString(7, null);
            } else {
                ps.setString(7, user.getInviteStatus().getCode());
            }

            ps.setString(8, user.getActive());
            return ps;

        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            user.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for User.");
            throw new DatabaseException("Error retrieving unique id for User.");

        }

        return user;
    }

    public void updateById(User u) {
        jdbcTemplate.update(UPDATE_USER_BY_ID, u.getFirstName(), u.getLastName(), u.getEmail(), u.getPassword(), u.getActive(), u.getId());
    }

    public void updateByUid(User u) {
        jdbcTemplate.update(UPDATE_USER_BY_UUID, u.getFirstName(), u.getLastName(), u.getEmail(), u.getPassword(), u.getActive(), u.getUid());
    }

    public void updateInactiveById(Long userId) {
        jdbcTemplate.update(UPDATE_USER_INACTIVE_BY_ID, userId);
    }

    public void delete(User u) {
        deleteById(u.getId());
    }

    public void deleteById(Long userId) {
        String sql = DELETE_USER + " WHERE USER_ID = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void deleteByUid(String userUid) {
        String sql = DELETE_USER + " WHERE USER_UUID = ?";
        jdbcTemplate.update(sql, userUid);
    }

}