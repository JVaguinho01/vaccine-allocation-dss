package com.vaccination.backend.repository;

import com.vaccination.backend.model.Region;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class RegionRepository {

    private final JdbcTemplate jdbcTemplate;

    public RegionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Region> findAll() {
        String sql = "SELECT id, name FROM regions ORDER BY name";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Region(
                UUID.fromString(rs.getString("id")),
                rs.getString("name")
        ));
    }

    public List<Region> findPage(String search, String sort, String dir, int page, int size) {
        String safeSort = "name";
        String safeDir  = "desc".equalsIgnoreCase(dir) ? "DESC" : "ASC";

        StringBuilder sql = new StringBuilder("SELECT id, name FROM regions WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + search.toLowerCase() + "%");
        }

        sql.append(" ORDER BY ").append(safeSort).append(" ").append(safeDir);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new Region(
                UUID.fromString(rs.getString("id")),
                rs.getString("name")
        ), params.toArray());
    }

    public long count(String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM regions WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + search.toLowerCase() + "%");
        }

        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return result != null ? result : 0L;
    }
}
