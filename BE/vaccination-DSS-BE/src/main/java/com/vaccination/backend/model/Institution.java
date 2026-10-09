package com.vaccination.backend.model;

import java.util.UUID;

public class Institution {

    private UUID id;
    private String name;
    private UUID regionId;
    private Integer dailyVaccinationRate;
    private Integer storageCapacity;
    private boolean removed;

    public Institution() {
    }

    public Institution(UUID id,
                       String name,
                       UUID regionId,
                       Integer dailyVaccinationRate,
                       Integer storageCapacity) {
        this.id = id;
        this.name = name;
        this.regionId = regionId;
        this.dailyVaccinationRate = dailyVaccinationRate;
        this.storageCapacity = storageCapacity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getRegionId() {
        return regionId;
    }

    public void setRegionId(UUID regionId) {
        this.regionId = regionId;
    }

    public Integer getDailyVaccinationRate() {
        return dailyVaccinationRate;
    }

    public void setDailyVaccinationRate(Integer dailyVaccinationRate) {
        this.dailyVaccinationRate = dailyVaccinationRate;
    }

    public Integer getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(Integer storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    public boolean isRemoved() { return removed; }
    public void setRemoved(boolean removed) { this.removed = removed; }

    public int getOperationalCapacity() {
        if (dailyVaccinationRate == null && storageCapacity == null) {
            return 0;
        }
        if (dailyVaccinationRate == null) {
            return storageCapacity;
        }
        if (storageCapacity == null) {
            return dailyVaccinationRate;
        }
        return Math.min(dailyVaccinationRate, storageCapacity);
    }
}