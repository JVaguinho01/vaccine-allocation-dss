package com.vaccination.backend.service;

import com.vaccination.backend.dto.InstitutionCriteriaDTO;
import com.vaccination.backend.repository.InstitutionRepository;
import com.vaccination.backend.repository.InstitutionRepository.PatientPref;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CriteriaCalculationService {

    private final InstitutionRepository institutionRepository;

    public CriteriaCalculationService(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    /**
     * Assigns each active patient to an institution, nearest first. When an
     * institution reaches its rate cap, the remaining patients move on to their
     * next-nearest option in the following round.
     */
    public List<InstitutionCriteriaDTO> calculateCriteria(UUID regionId,
                                                          int campaignDays,
                                                          double ratePct) {

        List<InstitutionCriteriaDTO> institutions =
                institutionRepository.findInstitutionCriteria(regionId);

        if (institutions.isEmpty()) return institutions;

        Map<UUID, Integer> rateCaps = new HashMap<>();
        for (InstitutionCriteriaDTO inst : institutions) {
            int cap = (int) Math.max(1,
                    Math.floor(inst.getDailyVaccinationRate() * ratePct * campaignDays));
            rateCaps.put(inst.getInstitutionId(), cap);
        }

        // Ordered by (patient_id, pref_rank) in the query.
        List<PatientPref> allPrefs = institutionRepository.findPatientPreferences(regionId);

        Map<UUID, List<PatientPref>> byPatient = new LinkedHashMap<>();
        for (PatientPref pref : allPrefs) {
            byPatient.computeIfAbsent(pref.patientId(), k -> new ArrayList<>()).add(pref);
        }

        Map<UUID, Integer> remaining = new HashMap<>(rateCaps);

        // [count, riskLevelSum, riskExposureSum]
        Map<UUID, double[]> demandStats = new HashMap<>();

        Map<UUID, Integer> nextPrefIdx = new HashMap<>();

        Set<UUID> toAssign = new LinkedHashSet<>(byPatient.keySet());

        while (!toAssign.isEmpty()) {

            Map<UUID, List<PatientPref>> proposals = new HashMap<>();

            for (UUID patientId : toAssign) {
                int idx = nextPrefIdx.getOrDefault(patientId, 0);
                List<PatientPref> prefs = byPatient.get(patientId);
                if (idx < prefs.size()) {
                    PatientPref p = prefs.get(idx);
                    proposals.computeIfAbsent(p.instId(), k -> new ArrayList<>()).add(p);
                    nextPrefIdx.put(patientId, idx + 1);
                }
            }

            Set<UUID> bumped = new LinkedHashSet<>();

            for (Map.Entry<UUID, List<PatientPref>> entry : proposals.entrySet()) {
                UUID instId   = entry.getKey();
                List<PatientPref> applicants = entry.getValue();
                int cap = remaining.getOrDefault(instId, 0);

                // Nearest applicants get the available places first.
                applicants.sort(Comparator.comparingDouble(PatientPref::distanceM));

                int accepted = 0;
                for (PatientPref applicant : applicants) {
                    if (accepted < cap) {
                        double[] stats = demandStats.computeIfAbsent(instId, k -> new double[3]);
                        stats[0]++;
                        stats[1] += applicant.riskLevel();
                        stats[2] += applicant.riskExposure();
                        accepted++;
                    } else {
                        bumped.add(applicant.patientId());
                    }
                }
                remaining.put(instId, cap - accepted);
            }

            toAssign = bumped.stream()
                    .filter(p -> nextPrefIdx.getOrDefault(p, 0) < byPatient.get(p).size())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        Map<UUID, InstitutionCriteriaDTO> instById = institutions.stream()
                .collect(Collectors.toMap(InstitutionCriteriaDTO::getInstitutionId, i -> i));

        for (Map.Entry<UUID, double[]> entry : demandStats.entrySet()) {
            InstitutionCriteriaDTO inst = instById.get(entry.getKey());
            if (inst == null) continue;
            double[] s = entry.getValue();
            double count = s[0];
            inst.setDemand(count);
            inst.setRiskLevel(   count > 0 ? s[1] / count : 0.0);
            inst.setRiskExposure(count > 0 ? s[2] / count : 0.0);
        }

        return institutions;
    }
}
