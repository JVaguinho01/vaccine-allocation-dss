package com.vaccination.backend.controller;

import com.vaccination.backend.dto.CreateInstitutionDTO;
import com.vaccination.backend.dto.InstitutionDTO;
import com.vaccination.backend.dto.PageResponseDTO;
import com.vaccination.backend.dto.UpdateInstitutionDTO;
import com.vaccination.backend.service.InstitutionService;
import com.vaccination.backend.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/institutions")
@CrossOrigin(origins = "*")
public class InstitutionController {

    private final InstitutionService institutionService;
    private final JwtUtil jwtUtil;

    public InstitutionController(InstitutionService institutionService, JwtUtil jwtUtil) {
        this.institutionService = institutionService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public PageResponseDTO<InstitutionDTO> getAll(
            @RequestParam(defaultValue = "")     String search,
            @RequestParam(defaultValue = "")     String regionId,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc")  String dir,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "50")   int size
    ) {
        return institutionService.getPage(search, regionId, sort, dir, page, size);
    }

    @PostMapping
    public InstitutionDTO create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateInstitutionDTO dto
    ) {
        requireAdmin(authHeader);
        return institutionService.create(dto);
    }

    @PutMapping("/{id}")
    public InstitutionDTO update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody UpdateInstitutionDTO dto
    ) {
        requireAdmin(authHeader);
        return institutionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ) {
        requireAdmin(authHeader);
        institutionService.delete(id);
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
