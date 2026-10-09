package com.vaccination.backend.controller;

import com.vaccination.backend.dto.AllocationResponseDTO;
import com.vaccination.backend.dto.DecisionRequestDTO;
import com.vaccination.backend.service.AllocationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/allocation")
@CrossOrigin(origins = "*")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping("/ahp")
    public AllocationResponseDTO allocate(
            @RequestBody DecisionRequestDTO request
    ) {
        return allocationService.allocate(request);
    }
}