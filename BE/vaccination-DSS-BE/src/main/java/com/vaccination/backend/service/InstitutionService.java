package com.vaccination.backend.service;

import com.vaccination.backend.dto.CreateInstitutionDTO;
import com.vaccination.backend.dto.InstitutionDTO;
import com.vaccination.backend.dto.PageResponseDTO;
import com.vaccination.backend.dto.UpdateInstitutionDTO;
import com.vaccination.backend.repository.InstitutionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    public InstitutionService(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    public PageResponseDTO<InstitutionDTO> getPage(String search, String regionId,
                                                    String sort, String dir, int page, int size) {
        List<InstitutionDTO> content = institutionRepository.findPage(search, regionId, sort, dir, page, size);
        long total = institutionRepository.count(search, regionId);
        return new PageResponseDTO<>(content, page, size, total);
    }

    public InstitutionDTO create(CreateInstitutionDTO dto) {
        validateCapacities(dto.getDailyVaccinationRate(), dto.getStorageCapacity());
        return institutionRepository.create(dto);
    }

    public InstitutionDTO update(UUID id, UpdateInstitutionDTO dto) {
        validateCapacities(dto.getDailyVaccinationRate(), dto.getStorageCapacity());
        return institutionRepository.update(id, dto);
    }

    private void validateCapacities(Integer dailyVaccinationRate, Integer storageCapacity) {
        if (dailyVaccinationRate != null && storageCapacity != null
                && storageCapacity < dailyVaccinationRate) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Storage capacity must be greater than or equal to daily vaccination rate."
            );
        }
    }

    public void delete(UUID id) {
        institutionRepository.softDelete(id);
    }
}
