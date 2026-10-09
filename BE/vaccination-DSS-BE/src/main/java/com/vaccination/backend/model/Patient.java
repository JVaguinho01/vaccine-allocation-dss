package com.vaccination.backend.model;

import java.time.LocalDate;
import java.util.UUID;

public class Patient {

    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private UUID regionId;
    private String address;
    private String zipCode;
    private Integer riskLevel;
    private Integer riskExposure;

    public Patient() {
    }

    public Patient(UUID id,
                   String firstName,
                   String lastName,
                   LocalDate birthDate,
                   String gender,
                   UUID regionId,
                   Integer riskLevel,
                   Integer riskExposure) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.regionId = regionId;
        this.riskLevel = riskLevel;
        this.riskExposure = riskExposure;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public UUID getRegionId() {
        return regionId;
    }

    public void setRegionId(UUID regionId) {
        this.regionId = regionId;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getRiskExposure() {
        return riskExposure;
    }

    public void setRiskExposure(Integer riskExposure) {
        this.riskExposure = riskExposure;
    }
}