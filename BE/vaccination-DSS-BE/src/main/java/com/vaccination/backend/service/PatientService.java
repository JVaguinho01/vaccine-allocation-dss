package com.vaccination.backend.service;

import com.vaccination.backend.dto.PageResponseDTO;
import com.vaccination.backend.dto.PatientDTO;
import com.vaccination.backend.dto.UpdatePatientDTO;
import com.vaccination.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PageResponseDTO<PatientDTO> getPage(String search, String regionId, String gender,
                                               String riskLevel, String riskExposure,
                                               String sort, String dir, int page, int size) {
        List<PatientDTO> content = patientRepository.findPage(
                search, regionId, gender, riskLevel, riskExposure, sort, dir, page, size);
        long total = patientRepository.count(search, regionId, gender, riskLevel, riskExposure);
        return new PageResponseDTO<>(content, page, size, total);
    }

    public PatientDTO update(UUID id, UpdatePatientDTO dto) {
        return patientRepository.update(id, dto);
    }

    public void delete(UUID id) {
        patientRepository.softDelete(id);
    }
}
