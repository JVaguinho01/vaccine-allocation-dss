package com.vaccination.backend.controller;

import com.vaccination.backend.dto.PageResponseDTO;
import com.vaccination.backend.dto.PatientDTO;
import com.vaccination.backend.dto.UpdatePatientDTO;
import com.vaccination.backend.service.PatientService;
import com.vaccination.backend.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;
    private final JwtUtil jwtUtil;

    public PatientController(PatientService patientService, JwtUtil jwtUtil) {
        this.patientService = patientService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public PageResponseDTO<PatientDTO> getAll(
            @RequestParam(defaultValue = "")         String search,
            @RequestParam(defaultValue = "")         String regionId,
            @RequestParam(defaultValue = "")         String gender,
            @RequestParam(defaultValue = "")         String riskLevel,
            @RequestParam(defaultValue = "")         String riskExposure,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc")      String dir,
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "50")       int size
    ) {
        return patientService.getPage(search, regionId, gender, riskLevel, riskExposure, sort, dir, page, size);
    }

    @PutMapping("/{id}")
    public PatientDTO update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody UpdatePatientDTO dto
    ) {
        requireAdmin(authHeader);
        return patientService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ) {
        requireAdmin(authHeader);
        patientService.delete(id);
    }

    private void requireAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token) || !"admin".equals(jwtUtil.extractRole(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
