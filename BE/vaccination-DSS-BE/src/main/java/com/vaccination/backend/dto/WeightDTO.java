package com.vaccination.backend.dto;

public class WeightDTO {

    private String criteria;
    private double weight;

    public WeightDTO() {
    }

    public WeightDTO(String criteria, double weight) {
        this.criteria = criteria;
        this.weight = weight;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}