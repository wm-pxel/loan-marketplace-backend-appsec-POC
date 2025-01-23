package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.InstitutionRowMapper;
import com.westmonroe.loansyndication.model.Institution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.InstitutionQueryDef.*;

@Repository
@Slf4j
public class InstitutionDao {

    private final JdbcTemplate jdbcTemplate;

    public InstitutionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Institution> findAll() {
        String sql = SELECT_INSTITUTION + " ORDER BY INSTITUTION_NAME";
        return jdbcTemplate.query(sql, new InstitutionRowMapper());
    }

    public List<Institution> findAllByInstitutionId(Long institutionId) {
        String sql = SELECT_INSTITUTION + " WHERE II.INSTITUTION_ID = ? ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new InstitutionRowMapper(), institutionId);
    }

    public List<Institution> findAllNotOnDeal(String dealUid) {
        String sql = SELECT_INSTITUTION + " WHERE II.INSTITUTION_ID NOT IN ( SELECT EP.PARTICIPANT_ID "
                                                                            + "FROM EVENT_PARTICIPANT EP LEFT JOIN EVENT_INFO EI "
                                                                              + "ON EP.EVENT_ID = EI.EVENT_ID LEFT JOIN DEAL_INFO DI "
                                                                              + "ON EI.DEAL_ID = DI.DEAL_ID "
                                                                           + "WHERE DI.DEAL_UUID = ? "
                                                                             + "AND EP.PARTICIPANT_ID IS NOT NULL ) "
                                           + "AND II.INSTITUTION_ID NOT IN ( SELECT ORIGINATOR_ID FROM DEAL_INFO WHERE DEAL_UUID = ? ) "
                                           + "AND II.ACTIVE_IND = 'Y' "
                                         + "ORDER BY II.INSTITUTION_NAME";
        return jdbcTemplate.query(sql, new InstitutionRowMapper(), dealUid, dealUid);
    }

    public List<Institution> findEventParticipantsNotOnDeal(String dealUid) {
        String sql = SELECT_INSTITUTION + " WHERE II.INSTITUTION_ID NOT IN ( SELECT EP.PARTICIPANT_ID "
                                                                            + "FROM EVENT_PARTICIPANT EP LEFT JOIN EVENT_INFO EI "
                                                                              + "ON EP.EVENT_ID = EI.EVENT_ID LEFT JOIN DEAL_INFO DI "
                                                                              + "ON EI.DEAL_ID = DI.DEAL_ID "
                                                                           + "WHERE DI.DEAL_UUID = ? "
                                                                             + "AND EP.PARTICIPANT_ID IS NOT NULL ) "
                                           + "AND II.INSTITUTION_ID NOT IN ( SELECT ORIGINATOR_ID FROM DEAL_INFO WHERE DEAL_UUID = ? ) "
                                           + "AND II.ACTIVE_IND = 'Y' "
                                         + "ORDER BY II.INSTITUTION_NAME";
        return jdbcTemplate.query(sql, new InstitutionRowMapper(), dealUid, dealUid);
    }

    public Institution findById(Long id) {
        String sql = SELECT_INSTITUTION + " WHERE II.INSTITUTION_ID = ?";
        Institution institution;

        try {
            institution = jdbcTemplate.queryForObject(sql, new InstitutionRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Institution was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Institution was not found for id.");

        }

        return institution;
    }

    public Institution findByUid(String uid) {

        String sql = SELECT_INSTITUTION + " WHERE II.INSTITUTION_UUID = ?";
        Institution institution;

        try {
            institution = jdbcTemplate.queryForObject(sql, new InstitutionRowMapper(), uid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Institution was not found for uid. ( uid = %s )", uid));
            throw new DataNotFoundException("Institution was not found for uid.");

        }

        return institution;
    }

    public Institution findByName(String name) {

        String sql = SELECT_INSTITUTION + " WHERE UPPER(II.INSTITUTION_NAME) = ?";
        Institution institution;

        try {
            institution = jdbcTemplate.queryForObject(sql, new InstitutionRowMapper(), name.toUpperCase());
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Institution was not found for name. ( name = %s )", name));
            throw new DataNotFoundException("Institution was not found for name.");

        }

        return institution;
    }

    public Institution findByDealExternalId(String dealExternalId) {
        String sql = SELECT_INSTITUTION + " WHERE II.INSTITUTION_ID = ( SELECT ORIGINATOR_ID "
                                                                       + "FROM DEAL_INFO "
                                                                      + "WHERE DEAL_EXTERNAL_UUID = ? )";
        Institution institution;

        try {
            institution = jdbcTemplate.queryForObject(sql, new InstitutionRowMapper(), dealExternalId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Institution was not found for deal external id. ( id = %s )", dealExternalId));
            throw new DataNotFoundException("Institution was not found for deal external id.");

        }

        return institution;
    }

    public Institution save(Institution institution) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_INSTITUTION, new String[] { "institution_id" });
            ps.setString(index++, institution.getUid());
            ps.setString(index++, institution.getName());
            ps.setString(index++, institution.getBrandName());
            ps.setString(index++, institution.getCommunityExtension());
            ps.setString(index++, institution.getCommunityName());
            ps.setString(index++, institution.getCommunityNetworkID());
            ps.setString(index++, institution.getLookupKey());
            ps.setString(index++, institution.getOwner());
            ps.setString(index++, institution.getPermissionSet());
            ps.setString(index, institution.getActive());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            institution.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Institution.");
            throw new DatabaseException("Error retrieving unique id for Institution.");

        }

        return institution;
    }

    public void update(Institution i) {
        jdbcTemplate.update(UPDATE_INSTITUTION, i.getName(), i.getBrandName(), i.getCommunityExtension()
                , i.getCommunityName(), i.getCommunityNetworkID(), i.getLookupKey(), i.getOwner()
                , i.getPermissionSet(), i.getActive(), i.getId());
    }

    public int deleteById(Long id) {
        String sql = DELETE_INSTITUTION + " WHERE INSTITUTION_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int deleteByUid(String uid) {
        String sql = DELETE_INSTITUTION + " WHERE INSTITUTION_UUID = ?";
        return jdbcTemplate.update(sql, uid);
    }

}