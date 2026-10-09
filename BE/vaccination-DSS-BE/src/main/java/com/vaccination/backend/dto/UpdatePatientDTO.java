package com.vaccination.backend.dto;

import java.util.UUID;

public class UpdatePatientDTO {

    private UUID regionId;
    private String address;
    private String zipCode;
    private Integer riskLevel;
    private Integer riskExposure;

    public UUID getRegionId() { return regionId; }
    public void setRegionId(UUID regionId) { this.regionId = regionId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public Integer getRiskLevel() { return riskLevel; }
    public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }

    public Integer getRiskExposure() { return riskExposure; }
    public void setRiskExposure(Integer riskExposure) { this.riskExposure = riskExposure; }
}
