package com.vaccination.backend.dto;

import java.util.UUID;

public class CreateInstitutionDTO {

    private String name;
    private UUID regionId;
    private Integer dailyVaccinationRate;
    private Integer storageCapacity;
    private String address;
    private String zipCode;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getRegionId() { return regionId; }
    public void setRegionId(UUID regionId) { this.regionId = regionId; }

    public Integer getDailyVaccinationRate() { return dailyVaccinationRate; }
    public void setDailyVaccinationRate(Integer dailyVaccinationRate) { this.dailyVaccinationRate = dailyVaccinationRate; }

    public Integer getStorageCapacity() { return storageCapacity; }
    public void setStorageCapacity(Integer storageCapacity) { this.storageCapacity = storageCapacity; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
}
