package com.vaccination.backend.dto;

import java.util.UUID;

public class PatientDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String gender;
    private UUID regionId;
    private String regionName;
    private String address;
    private String zipCode;
    private Integer riskLevel;
    private Integer riskExposure;
    private java.time.LocalDate birthDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public UUID getRegionId() { return regionId; }
    public void setRegionId(UUID regionId) { this.regionId = regionId; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public Integer getRiskLevel() { return riskLevel; }
    public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }

    public Integer getRiskExposure() { return riskExposure; }
    public void setRiskExposure(Integer riskExposure) { this.riskExposure = riskExposure; }

    public java.time.LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(java.time.LocalDate birthDate) { this.birthDate = birthDate; }
}
