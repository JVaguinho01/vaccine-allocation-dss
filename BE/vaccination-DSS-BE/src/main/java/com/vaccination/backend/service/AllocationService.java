package com.vaccination.backend.service;

import com.vaccination.backend.dto.AllocationResponseDTO;
import com.vaccination.backend.dto.AllocationResultDTO;
import com.vaccination.backend.dto.DecisionRequestDTO;
import com.vaccination.backend.dto.InstitutionCriteriaDTO;
import com.vaccination.backend.dto.WeightDTO;
import com.vaccination.backend.repository.InstitutionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AllocationService {

    private final CriteriaCalculationService criteriaCalculationService;
    private final AHPService ahpService;
    private final InstitutionRepository institutionRepository;

    public AllocationService(CriteriaCalculationService criteriaCalculationService,
                             AHPService ahpService,
                             InstitutionRepository institutionRepository) {
        this.criteriaCalculationService = criteriaCalculationService;
        this.ahpService = ahpService;
        this.institutionRepository = institutionRepository;
    }

    public AllocationResponseDTO allocate(DecisionRequestDTO request) {

        String pace         = request.getVaccinationPace();
        int    campaignDays = request.getCampaignDays();

        final double storagePct = switch (pace) {
            case "relaxed"  -> 0.30;
            case "standard" -> 0.55;
            default         -> 0.85;
        };
        final double ratePct = switch (pace) {
            case "relaxed"  -> 0.50;
            case "standard" -> 0.75;
            default         -> 1.00;
        };

        List<InstitutionCriteriaDTO> institutions =
                criteriaCalculationService.calculateCriteria(
                        request.getRegionId(), campaignDays, ratePct);

        if (institutions.isEmpty()) {
            AllocationResponseDTO response = new AllocationResponseDTO();
            response.setWeights(List.of());
            response.setAllocations(List.of());
            return response;
        }

        for (InstitutionCriteriaDTO inst : institutions) {
            double storageAlloc = inst.getStorageCapacity() * storagePct;
            double rateAlloc    = inst.getDailyVaccinationRate() * ratePct * campaignDays;
            double effectiveCap = Math.min(storageAlloc, rateAlloc);

            double wasteRisk = (storageAlloc > 0)
                    ? Math.max(0.0, 1.0 - effectiveCap / storageAlloc)
                    : 1.0;

            inst.setCapacity(effectiveCap);
            inst.setWasteRisk(wasteRisk);
        }

        AllocationResponseDTO ahpResponse =
                ahpService.calculateWeights(institutions, request.getCriteriaOrder());
        List<WeightDTO> weights = ahpResponse.getWeights();

        ahpService.applyScores(institutions, weights);

        // Capped by daily rate, not storage: restocks refill storage during the campaign.
        distributeVaccines(institutions, request.getTotalVaccinesAvailable(), ratePct, campaignDays);

        institutions.sort(
                Comparator.comparingDouble(InstitutionCriteriaDTO::getFinalScore).reversed());

        List<AllocationResultDTO> allocations = institutions.stream()
                .map(inst -> {
                    AllocationResultDTO dto = new AllocationResultDTO();
                    dto.setInstitutionId(inst.getInstitutionId());
                    dto.setInstitutionName(inst.getInstitutionName());
                    dto.setFinalScore(inst.getFinalScore());
                    dto.setAllocatedVaccines(inst.getAllocatedVaccines());

                    int completionDay = computeCompletionDay(
                            inst.getAllocatedVaccines(),
                            inst.getDailyVaccinationRate(),
                            ratePct);
                    dto.setCompletionDay(completionDay);

                    int effCap = (int) Math.round(
                            inst.getDailyVaccinationRate() * ratePct * completionDay);
                    dto.setEffectiveCapacity(effCap);

                    List<Integer> restockDays = computeRestockDays(
                            completionDay,
                            inst.getStorageCapacity(),
                            storagePct,
                            inst.getDailyVaccinationRate(),
                            ratePct);
                    dto.setRestockDays(restockDays);
                    dto.setRestockCount(restockDays.size());

                    int restockDoses = (int) Math.round(
                            inst.getStorageCapacity() * storagePct);
                    dto.setRestockDoses(restockDoses);

                    return dto;
                })
                .toList();

        int totalAllocated = allocations.stream()
                .mapToInt(AllocationResultDTO::getAllocatedVaccines)
                .sum();

        int totalPatients = institutionRepository.countActivePatients(request.getRegionId());

        AllocationResponseDTO response = new AllocationResponseDTO();
        response.setWeights(weights);
        response.setConsistency(ahpResponse.getConsistency());
        response.setAllocations(allocations);
        response.setTotalAllocated(totalAllocated);
        response.setTotalUnallocated(request.getTotalVaccinesAvailable() - totalAllocated);
        response.setTotalPatients(totalPatients);

        return response;
    }

    private void distributeVaccines(List<InstitutionCriteriaDTO> institutions,
                                    int totalVaccines,
                                    double ratePct,
                                    int campaignDays) {

        double totalScore = institutions.stream()
                .mapToDouble(InstitutionCriteriaDTO::getFinalScore)
                .sum();

        if (totalScore <= 0) return;

        int allocated = 0;

        for (InstitutionCriteriaDTO inst : institutions) {
            int proposed = (int) Math.round(
                    totalVaccines * (inst.getFinalScore() / totalScore));
            int capacity = rateCappedCapacity(inst, ratePct, campaignDays);
            int capped   = Math.min(proposed, capacity);
            inst.setAllocatedVaccines(capped);
            allocated += capped;
        }

        // Hand out leftover doses to the highest-scoring institutions with room left.
        int remaining = totalVaccines - allocated;
        institutions.sort(
                Comparator.comparingDouble(InstitutionCriteriaDTO::getFinalScore).reversed());

        while (remaining > 0) {
            boolean allocatedAny = false;
            for (InstitutionCriteriaDTO inst : institutions) {
                if (inst.getAllocatedVaccines() < rateCappedCapacity(inst, ratePct, campaignDays)) {
                    inst.setAllocatedVaccines(inst.getAllocatedVaccines() + 1);
                    remaining--;
                    allocatedAny = true;
                    if (remaining == 0) break;
                }
            }
            if (!allocatedAny) break;
        }
    }

    private int rateCappedCapacity(InstitutionCriteriaDTO inst,
                                   double ratePct,
                                   int campaignDays) {
        int rateCap = (int) Math.floor(inst.getDailyVaccinationRate() * ratePct * campaignDays);
        int demand  = (int) Math.ceil(inst.getDemand());
        return Math.min(rateCap, demand);
    }

    private int computeCompletionDay(int allocatedVaccines,
                                     int dailyRate,
                                     double ratePct) {
        double effectiveDaily = dailyRate * ratePct;
        if (effectiveDaily <= 0 || allocatedVaccines <= 0) return 0;
        return (int) Math.ceil(allocatedVaccines / effectiveDaily);
    }

    private List<Integer> computeRestockDays(int completionDay,
                                             int storageCapacity,
                                             double storagePct,
                                             int dailyRate,
                                             double ratePct) {
        if (completionDay <= 0 || storageCapacity <= 0 || dailyRate <= 0) return List.of();
        double effectiveStorage = storageCapacity * storagePct;
        double effectiveDaily   = dailyRate * ratePct;
        int daysPerCycle = (int) Math.floor(effectiveStorage / effectiveDaily);
        if (daysPerCycle <= 0) return List.of();

        List<Integer> days = new ArrayList<>();
        int day = daysPerCycle;
        while (day < completionDay) {
            days.add(day);
            day += daysPerCycle;
        }
        return days;
    }
}
