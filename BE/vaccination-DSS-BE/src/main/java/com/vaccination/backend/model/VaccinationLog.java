package com.vaccination.backend.model;

import java.time.LocalDate;
import java.util.UUID;

public class VaccinationLog {

    private UUID id;
    private UUID patientId;
    private UUID vaccineId;
    private UUID institutionId;
    private LocalDate vaccinationDate;
    private String status;

    public VaccinationLog() {
    }

    public VaccinationLog(UUID id,
                          UUID patientId,
                          UUID vaccineId,
                          UUID institutionId,
                          LocalDate vaccinationDate,
                          String status) {
        this.id = id;
        this.patientId = patientId;
        this.vaccineId = vaccineId;
        this.institutionId = institutionId;
        this.vaccinationDate = vaccinationDate;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(UUID vaccineId) {
        this.vaccineId = vaccineId;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
    }

    public LocalDate getVaccinationDate() {
        return vaccinationDate;
    }

    public void setVaccinationDate(LocalDate vaccinationDate) {
        this.vaccinationDate = vaccinationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}