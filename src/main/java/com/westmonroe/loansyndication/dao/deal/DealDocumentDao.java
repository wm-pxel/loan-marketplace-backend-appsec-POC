package com.westmonroe.loansyndication.dao.deal;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.exception.OperationNotAllowedException;
import com.westmonroe.loansyndication.mapper.deal.DealDocumentRowMapper;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.deal.DealDocumentQueryDef.*;

@Repository
@Slf4j
public class DealDocumentDao {

    private final JdbcTemplate jdbcTemplate;

    public DealDocumentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DealDocument> findAll() {
        throw new OperationNotAllowedException("The findAll() method is not implemented.");
    }

    public DealDocument findById(Long id) {
        String sql = SELECT_DEAL_DOCUMENT + " WHERE DD.DEAL_DOCUMENT_ID = ? ORDER BY DOCUMENT_CATEGORY_NAME, CREATED_DATE";
        DealDocument document;

        try {
            document = jdbcTemplate.queryForObject(sql, new DealDocumentRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal document was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Deal document was not found for id.");

        }

        return document;
    }

    public DealDocument findByExternalId(String documentExternalId) {
        String sql = SELECT_DEAL_DOCUMENT + " WHERE DD.DOCUMENT_EXTERNAL_UUID = ?";
        DealDocument document;

        try {
            document = jdbcTemplate.queryForObject(sql, new DealDocumentRowMapper(), documentExternalId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal document was not found for external id. ( id = %s )", documentExternalId));
            throw new DataNotFoundException("Deal document was not found for external id.");

        }

        return document;
    }

    public List<DealDocument> findAllByDealUid(String dealUid) {
        String sql = SELECT_DEAL_DOCUMENT + " WHERE DD.DEAL_ID = ( SELECT DEAL_ID "
                                                                  + "FROM DEAL_INFO "
                                                                 + "WHERE DEAL_UUID = ? ) "
                                             + "AND DEAL_DOCUMENT_IND = 'Y' "
                                           + "ORDER BY ORDER_NBR, CREATED_DATE";
        return jdbcTemplate.query(sql, new DealDocumentRowMapper(), dealUid);
    }

    public List<DealDocument> findAllByDealUidAndDisplayName(String dealUid, String displayName, String wildCardName) {
        String sql = SELECT_DEAL_DOCUMENT + " WHERE DD.DEAL_ID = ( SELECT DEAL_ID "
                                                                  + "FROM DEAL_INFO "
                                                                 + "WHERE DEAL_UUID = ? ) "
                                             + "AND ( DD.DISPLAY_NAME = ? OR DD.DISPLAY_NAME LIKE ? ) "
                                           + "ORDER BY DISPLAY_NAME";
        return jdbcTemplate.query(sql, new DealDocumentRowMapper(), dealUid, displayName, wildCardName);
    }

    public DealDocument findByDealUidAndDisplayName(String dealUid, String displayName) {
        String sql = SELECT_DEAL_DOCUMENT + " WHERE DD.DEAL_ID = ( SELECT DEAL_ID "
                                                                  + "FROM DEAL_INFO "
                                                                 + "WHERE DEAL_UUID = ? ) "
                                             + "AND DD.DISPLAY_NAME = ? "
                                           + "ORDER BY DISPLAY_NAME";
        DealDocument doc;

        try {
            doc = jdbcTemplate.queryForObject(sql, new DealDocumentRowMapper(), dealUid, displayName);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Document with displayName = %s was not found for this deal", displayName));
            throw new DataNotFoundException(String.format("Document with displayName = %s was not found for this deal", displayName));

        }

        return doc;
    }

    public DealDocument save(DealDocument document) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_DEAL_DOCUMENT, new String[] { "deal_document_id" });
            ps.setLong(index++, document.getDeal().getId());
            ps.setString(index++, document.getDisplayName());
            ps.setString(index++, document.getDocumentName());
            ps.setLong(index++, document.getCategory().getId());
            ps.setString(index++, document.getDocumentType());
            ps.setString(index++, document.getDescription());
            ps.setString(index++, document.getSource());
            ps.setString(index++, document.getDocumentExternalId());
            ps.setLong(index++, document.getCreatedBy().getId());
            ps.setLong(index, document.getCreatedBy().getId());

            return ps;

        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            document.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Deal Document.");
            throw new DatabaseException("Error retrieving unique id for Deal Document.");

        }

        return document;
    }

    public void update(DealDocument dd) {
        jdbcTemplate.update(UPDATE_DEAL_DOCUMENT, dd.getDisplayName(), dd.getDescription(), dd.getUpdatedBy().getId(), dd.getId());
    }

    public void deleteById(Long id) {
        String sql = DELETE_DEAL_DOCUMENT + " WHERE DEAL_DOCUMENT_ID = ?";
        jdbcTemplate.update(sql, id);
    }

    public void deleteAllByDealId(Long dealId) {
        String sql = DELETE_DEAL_DOCUMENT + " WHERE DEAL_ID = ?";
        jdbcTemplate.update(sql, dealId);
    }

    public void deleteAllByDealUid(String dealUid) {
        String sql = DELETE_DEAL_DOCUMENT + " WHERE DEAL_ID = ( SELECT DEAL_ID "
                                                               + "FROM DEAL_INFO "
                                                              + "WHERE DEAL_UUID = ? )";
        jdbcTemplate.update(sql, dealUid);
    }

    /**
     * This method will delete all deal documents from all deals, where the deal originator is the specified
     * institution.  This is used when an institution is deleted.
     *
     * @param   institutionId The id of the institution to be deleted.
     * @return  The number of rows deleted.
     */
    public int deleteAllByInstitutionId(Long institutionId) {
        String sql = DELETE_DEAL_DOCUMENT + " WHERE DEAL_ID IN ( SELECT DEAL_ID "
                                                                + "FROM DEAL_INFO "
                                                               + "WHERE ORIGINATOR_ID = ? )";
        return jdbcTemplate.update(sql, institutionId);
    }

    /**
     * This method will delete all deal documents from all deals, where the deal originator is the specified
     * institution.  This is used when an institution is deleted.
     *
     * @param   institutionUid The uid of the institution to be deleted.
     * @return  The number of rows deleted.
     */
    public int deleteAllByInstitutionUid(String institutionUid) {
        String sql = DELETE_DEAL_DOCUMENT + " WHERE DEAL_ID IN ( SELECT DI.DEAL_ID "
                                                                + "FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II "
                                                                  + "ON DI.ORIGINATOR_ID = II.INSTITUTION_ID "
                                                               + "WHERE II.INSTITUTION_UUID = ? )";
        return jdbcTemplate.update(sql, institutionUid);
    }

}