package com.westmonroe.loansyndication.dao.deal;

import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.deal.DealMemberRowMapper;
import com.westmonroe.loansyndication.model.deal.DealMember;
import com.westmonroe.loansyndication.utils.DealRelationEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.deal.DealMemberQueryDef.*;

@Repository
@Slf4j
public class DealMemberDao {

    private final JdbcTemplate jdbcTemplate;

    public DealMemberDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DealMember findByDealUidAndUserUid(String dealUid, String userUid) {
        String sql = SELECT_DEAL_MEMBER + " WHERE DI.DEAL_UUID = ? AND UI.USER_UUID = ?";
        DealMember dealMember;

        try {
            dealMember = jdbcTemplate.queryForObject(sql, new DealMemberRowMapper(), dealUid, userUid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal member was not found for deal ( uid = %s ) and user ( uid = %s ).", dealUid, userUid));
            throw new DataNotFoundException("Deal member was not found for deal and user.");

        } catch ( Exception e ) {

            log.error(e.getMessage());
            throw new RuntimeException("A runtime exception occurred.  Please check the logs for more information.");

        }

        return dealMember;
    }

    public List<DealMember> findAllByDealUid(String dealUid) {
        String sql = SELECT_DEAL_MEMBER + " WHERE DI.DEAL_UUID = ? ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new DealMemberRowMapper(), dealUid);
    }

    public List<DealMember> findAllByDealUidAndInstitutionUid(String dealUid, String institutionUid) {
        String sql = SELECT_DEAL_MEMBER + " WHERE DI.DEAL_UUID = ? AND UII.INSTITUTION_UUID = ? ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new DealMemberRowMapper(), dealUid, institutionUid);
    }

    public List<DealMember> findAllByDealUidAndMemberTypeCode(String dealUid, DealRelationEnum relation) {
        String sql = SELECT_DEAL_MEMBER + " WHERE DI.DEAL_UUID = ? AND DM.MEMBER_TYPE_CD = ? ORDER BY LAST_NAME, FIRST_NAME";
        return jdbcTemplate.query(sql, new DealMemberRowMapper(), dealUid, relation.getCode());
    }

    public void save(DealMember dm) {

        try {
            jdbcTemplate.update(INSERT_DEAL_MEMBER, dm.getDeal().getId(), dm.getUser().getId(), dm.getMemberTypeCode(), dm.getCreatedBy().getId());
        } catch ( DuplicateKeyException pke ) {

            log.error(String.format("Deal Member could not be saved because of DuplicateKeyException. ( deal uid = %s, user uid = %s )"
                    , dm.getDeal().getUid(), dm.getUser().getUid()));
            throw new DataIntegrityException("Deal Member could not be saved because it already exists.");

        } catch ( Exception e ) {

            log.error(e.getMessage());
            throw new RuntimeException("A runtime exception has occurred.  Please check the logs for more information");

        }
    }

    /**
     * This method will delete a specific member from the deal members table.
     *
     * @param   dealMember
     * @return  The number of rows deleted.
     */
    public int delete(DealMember dealMember) {
        String sql = DELETE_DEAL_MEMBER + " WHERE DEAL_ID = ( SELECT DEAL_ID FROM DEAL_INFO WHERE DEAL_UUID = ? ) "
                                           + "AND USER_ID = ( SELECT USER_ID FROM USER_INFO WHERE USER_UUID = ? )";

        return jdbcTemplate.update(sql, dealMember.getDeal().getUid(), dealMember.getUser().getUid());
    }

    /**
     * This method will delete all deal members from a deal.  This is used when a deal is deleted.
     *
     * @param   dealId
     * @return  The number of rows deleted.
     */
    public int deleteAllByDealId(Long dealId) {
        String sql = DELETE_DEAL_MEMBER + " WHERE DEAL_ID = ?";
        return jdbcTemplate.update(sql, dealId);
    }

    /**
     * This method will delete all deal members from a deal.  This is used when a deal is deleted.
     *
     * @param   dealUid
     * @return  The number of rows deleted.
     */
    public int deleteAllByDealUid(String dealUid) {
        String sql = DELETE_DEAL_MEMBER + " WHERE DEAL_ID = ( SELECT DEAL_ID FROM DEAL_INFO WHERE DEAL_UUID = ? )";
        return jdbcTemplate.update(sql, dealUid);
    }

    /**
     * This method will delete all deal members for the specified deal and institution uids.
     *
     * @param   dealUid        The uid of the deal.
     * @param   institutionUid The uid of the institution.
     * @return  The number of rows deleted.
     */
    public int deleteByDealUidAndInstitutionUid(String dealUid, String institutionUid) {
        String sql = DELETE_DEAL_MEMBER + " WHERE DEAL_ID = ( SELECT DEAL_ID FROM DEAL_INFO WHERE DEAL_UUID = ? ) "
                                           + "AND USER_ID IN ( SELECT UI.USER_ID "
                                                              + "FROM USER_INFO UI LEFT JOIN INSTITUTION_INFO II "
                                                                + "ON UI.INSTITUTION_ID = II.INSTITUTION_ID "
                                                             + "WHERE II.INSTITUTION_UUID = ? )";

        return jdbcTemplate.update(sql, institutionUid, institutionUid);
    }

    /**
     * This method will delete all deal members from all deals, where the deal originator or user institution is the
     * specified institution.  This is used when an institution is deleted.
     *
     * @param   institutionId The id of the institution to be deleted.
     * @return  The number of rows deleted.
     */
    public int deleteAllByInstitutionId(Long institutionId) {
        String sql = DELETE_DEAL_MEMBER + " WHERE DEAL_ID IN ( SELECT DEAL_ID "
                                                              + "FROM DEAL_INFO "
                                                             + "WHERE ORIGINATOR_ID = ? ) "
                                            + "OR USER_ID IN ( SELECT USER_ID "
                                                              + "FROM USER_INFO "
                                                             + "WHERE INSTITUTION_ID = ? )";

        return jdbcTemplate.update(sql, institutionId, institutionId);
    }

    /**
     * This method will delete all deal members from all deals, where the deal originator or user institution is the
     * specified institution.  This is used when an institution is deleted.
     *
     * @param   institutionUid The uid of the institution to be deleted.
     * @return  The number of rows deleted.
     */
    public int deleteAllByInstitutionUid(String institutionUid) {
        String sql = DELETE_DEAL_MEMBER + " WHERE DEAL_ID IN ( SELECT DI.DEAL_ID "
                                                              + "FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II "
                                                                + "ON DI.ORIGINATOR_ID = II.INSTITUTION_ID "
                                                             + "WHERE II.INSTITUTION_UUID = ? ) "
                                            + "OR USER_ID IN ( SELECT UI.USER_ID "
                                                              + "FROM USER_INFO UI LEFT JOIN INSTITUTION_INFO II "
                                                                + "ON UI.INSTITUTION_ID = II.INSTITUTION_ID "
                                                             + "WHERE II.INSTITUTION_UUID = ? )";

        return jdbcTemplate.update(sql, institutionUid, institutionUid);
    }

}