package com.vaccination.backend.repository;

import com.vaccination.backend.dto.PatientDTO;
import com.vaccination.backend.dto.UpdatePatientDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class PatientRepository {

    private final JdbcTemplate jdbcTemplate;

    public PatientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PatientDTO> findAllActive() {
        String sql = """
                SELECT p.id, p.first_name, p.last_name, p.gender,
                       p.region_id, r.name AS region_name,
                       p.address, p.zip_code,
                       p.risk_level, p.risk_exposure, p.birth_date
                FROM patients p
                JOIN regions r ON r.id = p.region_id
                WHERE p.is_active = true
                ORDER BY p.last_name, p.first_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PatientDTO dto = new PatientDTO();
            dto.setId(rs.getObject("id", UUID.class));
            dto.setFirstName(rs.getString("first_name"));
            dto.setLastName(rs.getString("last_name"));
            dto.setGender(rs.getString("gender"));
            dto.setRegionId(rs.getObject("region_id", UUID.class));
            dto.setRegionName(rs.getString("region_name"));
            dto.setAddress(rs.getString("address"));
            dto.setZipCode(rs.getString("zip_code"));
            dto.setRiskLevel(rs.getObject("risk_level", Integer.class));
            dto.setRiskExposure(rs.getObject("risk_exposure", Integer.class));
            var bd = rs.getDate("birth_date");
            if (bd != null) dto.setBirthDate(bd.toLocalDate());
            return dto;
        });
    }

    public PatientDTO update(UUID id, UpdatePatientDTO dto) {
        jdbcTemplate.update(
                "UPDATE patients SET region_id = ?, address = ?, zip_code = ?, risk_level = ?, risk_exposure = ? WHERE id = ? AND is_active = true",
                dto.getRegionId(), dto.getAddress(), dto.getZipCode(),
                dto.getRiskLevel(), dto.getRiskExposure(), id
        );
        return findById(id);
    }

    public void softDelete(UUID id) {
        jdbcTemplate.update(
                "UPDATE patients SET is_active = false WHERE id = ?",
                id
        );
    }

    public List<PatientDTO> findPage(String search, String regionId, String gender,
                                     String riskLevel, String riskExposure,
                                     String sort, String dir, int page, int size) {
        String safeSort = switch (sort != null ? sort : "") {
            case "gender"     -> "p.gender";
            case "regionName" -> "r.name";
            default           -> "p.last_name, p.first_name";
        };
        String safeDir = "desc".equalsIgnoreCase(dir) ? "DESC" : "ASC";

        StringBuilder sql = new StringBuilder("""
                SELECT p.id, p.first_name, p.last_name, p.gender,
                       p.region_id, r.name AS region_name,
                       p.address, p.zip_code, p.risk_level, p.risk_exposure, p.birth_date
                FROM patients p
                JOIN regions r ON r.id = p.region_id
                WHERE p.is_active = true
                """);
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(p.first_name) LIKE ? OR LOWER(p.last_name) LIKE ? OR LOWER(CONCAT(p.first_name, ' ', p.last_name)) LIKE ?)");
            String term = "%" + search.toLowerCase() + "%";
            params.add(term); params.add(term); params.add(term);
        }
        if (regionId != null && !regionId.isBlank()) {
            sql.append(" AND p.region_id = ?::uuid");
            params.add(regionId);
        }
        if (gender != null && !gender.isBlank()) {
            sql.append(" AND p.gender = ?");
            params.add(gender);
        }
        appendRiskFilter(sql, params, "p.risk_level", riskLevel);
        appendRiskFilter(sql, params, "p.risk_exposure", riskExposure);

        // For compound sort, apply direction to each part
        if ("p.last_name, p.first_name".equals(safeSort)) {
            sql.append(" ORDER BY p.last_name ").append(safeDir).append(", p.first_name ").append(safeDir);
        } else {
            sql.append(" ORDER BY ").append(safeSort).append(" ").append(safeDir);
        }
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            PatientDTO dto = new PatientDTO();
            dto.setId(rs.getObject("id", UUID.class));
            dto.setFirstName(rs.getString("first_name"));
            dto.setLastName(rs.getString("last_name"));
            dto.setGender(rs.getString("gender"));
            dto.setRegionId(rs.getObject("region_id", UUID.class));
            dto.setRegionName(rs.getString("region_name"));
            dto.setAddress(rs.getString("address"));
            dto.setZipCode(rs.getString("zip_code"));
            dto.setRiskLevel(rs.getObject("risk_level", Integer.class));
            dto.setRiskExposure(rs.getObject("risk_exposure", Integer.class));
            var bd = rs.getDate("birth_date");
            if (bd != null) dto.setBirthDate(bd.toLocalDate());
            return dto;
        }, params.toArray());
    }

    public long count(String search, String regionId, String gender,
                      String riskLevel, String riskExposure) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM patients p
                JOIN regions r ON r.id = p.region_id
                WHERE p.is_active = true
                """);
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(p.first_name) LIKE ? OR LOWER(p.last_name) LIKE ? OR LOWER(CONCAT(p.first_name, ' ', p.last_name)) LIKE ?)");
            String term = "%" + search.toLowerCase() + "%";
            params.add(term); params.add(term); params.add(term);
        }
        if (regionId != null && !regionId.isBlank()) {
            sql.append(" AND p.region_id = ?::uuid");
            params.add(regionId);
        }
        if (gender != null && !gender.isBlank()) {
            sql.append(" AND p.gender = ?");
            params.add(gender);
        }
        appendRiskFilter(sql, params, "p.risk_level", riskLevel);
        appendRiskFilter(sql, params, "p.risk_exposure", riskExposure);

        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return result != null ? result : 0L;
    }

    private void appendRiskFilter(StringBuilder sql, List<Object> params, String col, String level) {
        if (level == null || level.isBlank()) return;
        switch (level.toLowerCase()) {
            case "low"    -> sql.append(" AND ").append(col).append(" <= 2");
            case "medium" -> sql.append(" AND ").append(col).append(" = 3");
            case "high"   -> sql.append(" AND ").append(col).append(" >= 4");
        }
    }

    private PatientDTO findById(UUID id) {
        String sql = """
                SELECT p.id, p.first_name, p.last_name, p.gender,
                       p.region_id, r.name AS region_name,
                       p.address, p.zip_code,
                       p.risk_level, p.risk_exposure, p.birth_date
                FROM patients p
                JOIN regions r ON r.id = p.region_id
                WHERE p.id = ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            PatientDTO dto = new PatientDTO();
            dto.setId(rs.getObject("id", UUID.class));
            dto.setFirstName(rs.getString("first_name"));
            dto.setLastName(rs.getString("last_name"));
            dto.setGender(rs.getString("gender"));
            dto.setRegionId(rs.getObject("region_id", UUID.class));
            dto.setRegionName(rs.getString("region_name"));
            dto.setAddress(rs.getString("address"));
            dto.setZipCode(rs.getString("zip_code"));
            dto.setRiskLevel(rs.getObject("risk_level", Integer.class));
            dto.setRiskExposure(rs.getObject("risk_exposure", Integer.class));
            var bd = rs.getDate("birth_date");
            if (bd != null) dto.setBirthDate(bd.toLocalDate());
            return dto;
        }, id);
    }
}
