package com.vaccination.backend.dto;

import java.util.UUID;

public class InstitutionDTO {

    private UUID id;
    private String name;
    private UUID regionId;
    private String regionName;
    private Integer dailyVaccinationRate;
    private Integer storageCapacity;
    private String address;
    private String zipCode;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getRegionId() { return regionId; }
    public void setRegionId(UUID regionId) { this.regionId = regionId; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public Integer getDailyVaccinationRate() { return dailyVaccinationRate; }
    public void setDailyVaccinationRate(Integer dailyVaccinationRate) { this.dailyVaccinationRate = dailyVaccinationRate; }

    public Integer getStorageCapacity() { return storageCapacity; }
    public void setStorageCapacity(Integer storageCapacity) { this.storageCapacity = storageCapacity; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
}
