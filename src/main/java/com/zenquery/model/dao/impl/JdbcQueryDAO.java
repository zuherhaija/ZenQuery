package com.zenquery.model.dao.impl;

import com.zenquery.model.Query;
import com.zenquery.model.dao.QueryDAO;
import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by willy on 13.04.14.
 */
public class JdbcQueryDAO implements QueryDAO {
    private static final Logger logger = Logger.getLogger(QueryDAO.class);

    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Cacheable("sql.queries")
    public Query find(Integer id) {
        String sql = "SELECT * FROM queries WHERE id = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);

        Query query =
                jdbcTemplate.query(sql, new Object[] { id }, new QueryMapper()).get(0);

        return query;
    }

    @Cacheable("sql.queries")
    public List<Query> findByDatabaseConnectionId(Integer id) {
        String sql = "SELECT * FROM queries WHERE database_connection_id = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);

        List<Query> queries =
                jdbcTemplate.query(sql, new Object[] { id }, new QueryMapper());

        return queries;
    }

    public List<Query> findAll() {
        String sql = "SELECT * FROM queries";

        jdbcTemplate = new JdbcTemplate(dataSource);

        List<Query> queries =
                jdbcTemplate.query(sql, new QueryMapper());

        return queries;
    }

    public Number insert(Query query) {
        String sql = "INSERT INTO queries (key) VALUES (?)";

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(query.getKey().getBytes("UTF-8"));
            query.setKey(new String(messageDigest.digest()));
        } catch (Exception e) {
            logger.debug(e);
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, new Object[] {
                query.getKey()
        }, keyHolder);

        return keyHolder.getKey();
    }

    public void update(Integer id, Query query) {
        String sql = "UPDATE queries SET key = ? WHERE id = ?";

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(query.getKey().getBytes("UTF-8"));
            query.setKey(new String(messageDigest.digest()));
        } catch (Exception e) {
            logger.debug(e);
        }

        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.update(sql, new Object[]{
                query.getKey(),
                id
        });
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM queries WHERE id = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.update(sql);
    }

    private static class QueryMapper implements ParameterizedRowMapper<Query> {
        public Query mapRow(ResultSet rs, int rowNum) throws SQLException {
            Query query = new Query();

            query.setId(rs.getInt("id"));
            query.setKey(rs.getString("key"));
            query.setDatabaseConnectionId(rs.getInt("database_connection_id"));

            return query;
        }
    }
}
