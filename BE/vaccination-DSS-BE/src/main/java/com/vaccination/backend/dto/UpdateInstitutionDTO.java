package com.vaccination.backend.dto;

public class UpdateInstitutionDTO {

    private String name;
    private Integer dailyVaccinationRate;
    private Integer storageCapacity;
    private String address;
    private String zipCode;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDailyVaccinationRate() { return dailyVaccinationRate; }
    public void setDailyVaccinationRate(Integer dailyVaccinationRate) { this.dailyVaccinationRate = dailyVaccinationRate; }

    public Integer getStorageCapacity() { return storageCapacity; }
    public void setStorageCapacity(Integer storageCapacity) { this.storageCapacity = storageCapacity; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
}
