package com.vaccination.backend.repository;

import com.vaccination.backend.dto.CreateInstitutionDTO;
import com.vaccination.backend.dto.InstitutionCriteriaDTO;
import com.vaccination.backend.dto.InstitutionDTO;
import com.vaccination.backend.dto.UpdateInstitutionDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class InstitutionRepository {

    private final JdbcTemplate jdbcTemplate;

    public InstitutionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<InstitutionDTO> findAll() {
        String sql = """
                SELECT i.id, i.name, i.region_id, r.name AS region_name,
                       i.daily_vaccination_rate, i.storage_capacity, i.address, i.zip_code
                FROM institutions i
                JOIN regions r ON r.id = i.region_id
                WHERE i.removed IS NOT TRUE
                ORDER BY i.name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            InstitutionDTO dto = new InstitutionDTO();
            dto.setId(rs.getObject("id", UUID.class));
            dto.setName(rs.getString("name"));
            dto.setRegionId(rs.getObject("region_id", UUID.class));
            dto.setRegionName(rs.getString("region_name"));
            dto.setDailyVaccinationRate(rs.getObject("daily_vaccination_rate", Integer.class));
            dto.setStorageCapacity(rs.getObject("storage_capacity", Integer.class));
            dto.setAddress(rs.getString("address"));
            dto.setZipCode(rs.getString("zip_code"));
            return dto;
        });
    }

    public InstitutionDTO create(CreateInstitutionDTO dto) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO institutions (id, name, region_id, daily_vaccination_rate, storage_capacity, address, zip_code) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, dto.getName(), dto.getRegionId(), dto.getDailyVaccinationRate(), dto.getStorageCapacity(), dto.getAddress(), dto.getZipCode()
        );
        return findById(id);
    }

    public InstitutionDTO update(UUID id, UpdateInstitutionDTO dto) {
        jdbcTemplate.update(
                "UPDATE institutions SET name = ?, daily_vaccination_rate = ?, storage_capacity = ?, address = ?, zip_code = ? WHERE id = ? AND removed IS NOT TRUE",
                dto.getName(), dto.getDailyVaccinationRate(), dto.getStorageCapacity(), dto.getAddress(), dto.getZipCode(), id
        );
        return findById(id);
    }

    public void softDelete(UUID id) {
        jdbcTemplate.update(
                "UPDATE institutions SET removed = true WHERE id = ?",
                id
        );
    }

    public List<InstitutionDTO> findPage(String search, String regionId,
                                         String sort, String dir, int page, int size) {
        String safeSort = switch (sort != null ? sort : "") {
            case "regionName"         -> "r.name";
            case "storageCapacity"    -> "i.storage_capacity";
            case "dailyVaccinationRate" -> "i.daily_vaccination_rate";
            default                   -> "i.name";
        };
        String safeDir = "desc".equalsIgnoreCase(dir) ? "DESC" : "ASC";

        StringBuilder sql = new StringBuilder("""
                SELECT i.id, i.name, i.region_id, r.name AS region_name,
                       i.daily_vaccination_rate, i.storage_capacity, i.address, i.zip_code
                FROM institutions i
                JOIN regions r ON r.id = i.region_id
                WHERE i.removed IS NOT TRUE
                """);
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND LOWER(i.name) LIKE ?");
            params.add("%" + search.toLowerCase() + "%");
        }
        if (regionId != null && !regionId.isBlank()) {
            sql.append(" AND i.region_id = ?::uuid");
            params.add(regionId);
        }

        sql.append(" ORDER BY ").append(safeSort).append(" ").append(safeDir);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            InstitutionDTO dto = new InstitutionDTO();
            dto.setId(rs.getObject("id", UUID.class));
            dto.setName(rs.getString("name"));
            dto.setRegionId(rs.getObject("region_id", UUID.class));
            dto.setRegionName(rs.getString("region_name"));
            dto.setDailyVaccinationRate(rs.getObject("daily_vaccination_rate", Integer.class));
            dto.setStorageCapacity(rs.getObject("storage_capacity", Integer.class));
            dto.setAddress(rs.getString("address"));
            dto.setZipCode(rs.getString("zip_code"));
            return dto;
        }, params.toArray());
    }

    public long count(String search, String regionId) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM institutions i
                JOIN regions r ON r.id = i.region_id
                WHERE i.removed IS NOT TRUE
                """);
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND LOWER(i.name) LIKE ?");
            params.add("%" + search.toLowerCase() + "%");
        }
        if (regionId != null && !regionId.isBlank()) {
            sql.append(" AND i.region_id = ?::uuid");
            params.add(regionId);
        }

        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return result != null ? result : 0L;
    }

    private InstitutionDTO findById(UUID id) {
        String sql = """
                SELECT i.id, i.name, i.region_id, r.name AS region_name,
                       i.daily_vaccination_rate, i.storage_capacity, i.address, i.zip_code
                FROM institutions i
                JOIN regions r ON r.id = i.region_id
                WHERE i.id = ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            InstitutionDTO dto = new InstitutionDTO();
            dto.setId(rs.getObject("id", UUID.class));
            dto.setName(rs.getString("name"));
            dto.setRegionId(rs.getObject("region_id", UUID.class));
            dto.setRegionName(rs.getString("region_name"));
            dto.setDailyVaccinationRate(rs.getObject("daily_vaccination_rate", Integer.class));
            dto.setStorageCapacity(rs.getObject("storage_capacity", Integer.class));
            dto.setAddress(rs.getString("address"));
            dto.setZipCode(rs.getString("zip_code"));
            return dto;
        }, id);
    }

    public int countActivePatients(UUID regionId) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM patients WHERE region_id = ? AND is_active = true",
                Integer.class, regionId);
        return n != null ? n : 0;
    }

    public record PatientPref(
            UUID   patientId,
            double riskLevel,
            double riskExposure,
            UUID   instId,
            int    prefRank,
            double distanceM) {}

    // Demand and risk values start at 0 and are filled in by CriteriaCalculationService.
    public List<InstitutionCriteriaDTO> findInstitutionCriteria(UUID regionId) {
        String sql = """
                WITH warehouse AS (
                    SELECT location
                    FROM   institutions
                    WHERE  removed IS NOT TRUE AND location IS NOT NULL
                    ORDER  BY storage_capacity DESC NULLS LAST
                    LIMIT  1
                )
                SELECT
                    i.id                                                                         AS institution_id,
                    i.name                                                                       AS institution_name,
                    0.0                                                                          AS demand,
                    0.0                                                                          AS risk_level,
                    0.0                                                                          AS risk_exposure,
                    COALESCE(ST_Distance(i.location::geography, w.location::geography)
                             / 1000.0 * 0.50, 0)                                                AS logistic_cost,
                    COALESCE(i.daily_vaccination_rate, 0)                                        AS daily_vaccination_rate,
                    COALESCE(i.storage_capacity,       0)                                        AS storage_capacity
                FROM  institutions i
                CROSS JOIN warehouse w
                WHERE i.region_id = ?
                  AND i.removed  IS NOT TRUE
                ORDER BY i.name
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    InstitutionCriteriaDTO dto = new InstitutionCriteriaDTO();
                    dto.setInstitutionId(rs.getObject("institution_id", UUID.class));
                    dto.setInstitutionName(rs.getString("institution_name"));
                    dto.setDemand(rs.getDouble("demand"));
                    dto.setRiskExposure(rs.getDouble("risk_exposure"));
                    dto.setRiskLevel(rs.getDouble("risk_level"));
                    dto.setLogisticCost(rs.getDouble("logistic_cost"));
                    dto.setDailyVaccinationRate(rs.getInt("daily_vaccination_rate"));
                    dto.setStorageCapacity(rs.getInt("storage_capacity"));
                    return dto;
                },
                regionId);
    }

    public List<PatientPref> findPatientPreferences(UUID regionId) {
        String sql = """
                SELECT
                    p.id                                                                       AS patient_id,
                    p.risk_level,
                    p.risk_exposure,
                    i.id                                                                       AS inst_id,
                    ROW_NUMBER() OVER (
                        PARTITION BY p.id
                        ORDER BY p.location::geography <-> i.location::geography
                    )                                                                          AS pref_rank,
                    p.location::geography <-> i.location::geography                            AS distance_m
                FROM patients p
                JOIN institutions i
                     ON  i.region_id              = p.region_id
                     AND i.removed               IS NOT TRUE
                     AND i.location              IS NOT NULL
                     AND i.daily_vaccination_rate > 0
                WHERE p.is_active = true
                  AND p.region_id = ?
                ORDER BY p.id, pref_rank
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new PatientPref(
                        rs.getObject("patient_id",   UUID.class),
                        rs.getDouble("risk_level"),
                        rs.getDouble("risk_exposure"),
                        rs.getObject("inst_id",      UUID.class),
                        rs.getInt("pref_rank"),
                        rs.getDouble("distance_m")),
                regionId);
    }
}
