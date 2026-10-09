package com.vaccination.backend.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Vaccine {

    private UUID id;
    private String name;
    private BigDecimal cost;

    public Vaccine() {
    }

    public Vaccine(UUID id, String name, BigDecimal cost) {
        this.id = id;
        this.name = name;
        this.cost = cost;
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

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}