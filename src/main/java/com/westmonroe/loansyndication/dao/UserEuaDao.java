package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.UserEuaRowMapper;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.UserEua;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import static com.westmonroe.loansyndication.querydef.UserEuaQueryDef.INSERT_USER_EUA;
import static com.westmonroe.loansyndication.querydef.UserEuaQueryDef.SELECT_USER_EUA;
@Repository
@Slf4j
public class UserEuaDao {

    private final JdbcTemplate jdbcTemplate;

    public UserEuaDao(JdbcTemplate jdbcTemplate){ this.jdbcTemplate = jdbcTemplate; }

    public UserEua findUserEuaByUserId(Long userId){
        String sql = SELECT_USER_EUA + "WHERE UEX.USER_ID = ? ORDER BY UEX.AGREEMENT_DATE DESC LIMIT 1";

        try {
            return jdbcTemplate.queryForObject(sql, new UserEuaRowMapper(), userId);
        } catch ( EmptyResultDataAccessException e ){
            log.error(String.format("User EUA was not found for user id ( id = %s )", userId));
            throw new DataNotFoundException("User EUA was not found for user id");
        }
    }

    public boolean save(User user, Integer euaId){
        try {
            int rowsAffected = jdbcTemplate.update(INSERT_USER_EUA, user.getId(), euaId);
            return rowsAffected > 0;
        } catch ( DuplicateKeyException pke ) {
            log.error("User EUA could not be saved because of DuplicateKeyException. " +
                    "(user id = {}, euaId = {})", user.getId(), euaId);
            throw new DataIntegrityException("User EUA could not be saved because it already exists.");
        }
    }
}
