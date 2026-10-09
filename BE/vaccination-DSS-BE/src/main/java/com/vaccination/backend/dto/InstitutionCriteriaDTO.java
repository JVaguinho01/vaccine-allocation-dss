package com.vaccination.backend.dto;

import java.util.UUID;

public class InstitutionCriteriaDTO {

    private UUID institutionId;
    private String institutionName;

    private double demand;
    private double riskLevel;
    private double riskExposure;
    private double logisticCost;

    private int dailyVaccinationRate;
    private int storageCapacity;

    private double capacity;
    private double wasteRisk;

    private double finalScore;
    private int allocatedVaccines;

    public int getDailyVaccinationRate() { return dailyVaccinationRate; }
    public void setDailyVaccinationRate(int dailyVaccinationRate) { this.dailyVaccinationRate = dailyVaccinationRate; }

    public int getStorageCapacity() { return storageCapacity; }
    public void setStorageCapacity(int storageCapacity) { this.storageCapacity = storageCapacity; }

    public double getRiskExposure() {
        return riskExposure;
    }

    public void setRiskExposure(double riskExposure) {
        this.riskExposure = riskExposure;
    }

    public double getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(double riskLevel) {
        this.riskLevel = riskLevel;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public double getDemand() {
        return demand;
    }

    public void setDemand(double demand) {
        this.demand = demand;
    }

    public double getLogisticCost() {
        return logisticCost;
    }

    public void setLogisticCost(double logisticCost) {
        this.logisticCost = logisticCost;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getWasteRisk() {
        return wasteRisk;
    }

    public void setWasteRisk(double wasteRisk) {
        this.wasteRisk = wasteRisk;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public int getAllocatedVaccines() {
        return allocatedVaccines;
    }

    public void setAllocatedVaccines(int allocatedVaccines) {
        this.allocatedVaccines = allocatedVaccines;
    }

}