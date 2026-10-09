package com.vaccination.backend.dto;

public class ConsistencyDTO {

    private double consistencyIndex;

    private double consistencyRatio;

    private boolean consistent;

    public ConsistencyDTO() {
    }

    public ConsistencyDTO(
            double consistencyIndex,
            double consistencyRatio,
            boolean consistent
    ) {
        this.consistencyIndex = consistencyIndex;
        this.consistencyRatio = consistencyRatio;
        this.consistent = consistent;
    }

    public double getConsistencyIndex() {
        return consistencyIndex;
    }

    public void setConsistencyIndex(double consistencyIndex) {
        this.consistencyIndex = consistencyIndex;
    }

    public double getConsistencyRatio() {
        return consistencyRatio;
    }

    public void setConsistencyRatio(double consistencyRatio) {
        this.consistencyRatio = consistencyRatio;
    }

    public boolean isConsistent() {
        return consistent;
    }

    public void setConsistent(boolean consistent) {
        this.consistent = consistent;
    }
}