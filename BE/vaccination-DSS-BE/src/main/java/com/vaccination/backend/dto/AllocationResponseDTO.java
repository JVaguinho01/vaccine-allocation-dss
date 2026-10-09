package com.vaccination.backend.dto;

import java.util.List;

public class AllocationResponseDTO {

    private List<WeightDTO> weights;
    private List<AllocationResultDTO> allocations;
    private ConsistencyDTO consistency;
    private int totalAllocated;
    private int totalUnallocated;
    private int totalPatients;

    public AllocationResponseDTO() {
    }

    public ConsistencyDTO getConsistency() {
        return consistency;
    }

    public void setConsistency(ConsistencyDTO consistency) {
        this.consistency = consistency;
    }

    public List<WeightDTO> getWeights() {
        return weights;
    }

    public void setWeights(List<WeightDTO> weights) {
        this.weights = weights;
    }

    public List<AllocationResultDTO> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<AllocationResultDTO> allocations) {
        this.allocations = allocations;
    }

    public int getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(int totalAllocated) {
        this.totalAllocated = totalAllocated;
    }

    public int getTotalUnallocated() {
        return totalUnallocated;
    }

    public void setTotalUnallocated(int totalUnallocated) {
        this.totalUnallocated = totalUnallocated;
    }

    public int getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(int totalPatients) {
        this.totalPatients = totalPatients;
    }
}