package com.vaccination.backend.dto;

import java.util.List;
import java.util.UUID;

public class DecisionRequestDTO {

    private UUID regionId;
    private int totalVaccinesAvailable;
    private List<Integer> criteriaOrder;
    private int campaignDays;
    private String vaccinationPace; // "relaxed" | "standard" | "intensive"

    public DecisionRequestDTO() {
    }

    public UUID getRegionId() {
        return regionId;
    }

    public void setRegionId(UUID regionId) {
        this.regionId = regionId;
    }

    public int getTotalVaccinesAvailable() {
        return totalVaccinesAvailable;
    }

    public void setTotalVaccinesAvailable(int totalVaccinesAvailable) {
        this.totalVaccinesAvailable = totalVaccinesAvailable;
    }

    public List<Integer> getCriteriaOrder() {
        return criteriaOrder;
    }

    public void setCriteriaOrder(List<Integer> criteriaOrder) {
        this.criteriaOrder = criteriaOrder;
    }

    public int getCampaignDays() {
        return campaignDays > 0 ? campaignDays : 30;
    }

    public void setCampaignDays(int campaignDays) {
        this.campaignDays = campaignDays;
    }

    public String getVaccinationPace() {
        return vaccinationPace != null ? vaccinationPace : "intensive";
    }

    public void setVaccinationPace(String vaccinationPace) {
        this.vaccinationPace = vaccinationPace;
    }
}