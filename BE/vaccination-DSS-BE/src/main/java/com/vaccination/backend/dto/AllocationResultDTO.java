package com.vaccination.backend.dto;

import java.util.List;
import java.util.UUID;

public class AllocationResultDTO {

    private UUID institutionId;
    private String institutionName;
    private double finalScore;
    private int allocatedVaccines;

    private int effectiveCapacity;

    private int completionDay;

    private List<Integer> restockDays;

    private int restockCount;

    private int restockDoses;

    public AllocationResultDTO() {}

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }

    public int getAllocatedVaccines() { return allocatedVaccines; }
    public void setAllocatedVaccines(int allocatedVaccines) { this.allocatedVaccines = allocatedVaccines; }

    public int getEffectiveCapacity() { return effectiveCapacity; }
    public void setEffectiveCapacity(int effectiveCapacity) { this.effectiveCapacity = effectiveCapacity; }

    public int getCompletionDay() { return completionDay; }
    public void setCompletionDay(int completionDay) { this.completionDay = completionDay; }

    public List<Integer> getRestockDays() { return restockDays; }
    public void setRestockDays(List<Integer> restockDays) { this.restockDays = restockDays; }

    public int getRestockCount() { return restockCount; }
    public void setRestockCount(int restockCount) { this.restockCount = restockCount; }

    public int getRestockDoses() { return restockDoses; }
    public void setRestockDoses(int restockDoses) { this.restockDoses = restockDoses; }
}
