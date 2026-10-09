package com.vaccination.backend.controller;

import com.vaccination.backend.dto.PageResponseDTO;
import com.vaccination.backend.model.Region;
import com.vaccination.backend.repository.RegionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RegionController {

    private final RegionRepository regionRepository;

    public RegionController(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @GetMapping("/regions")
    public List<Region> getAllRegions() {
        return regionRepository.findAll();
    }

    @GetMapping("/regions/page")
    public PageResponseDTO<Region> getPage(
            @RequestParam(defaultValue = "")    String search,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc")  String dir,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "50")   int size
    ) {
        List<Region> content = regionRepository.findPage(search, sort, dir, page, size);
        long total = regionRepository.count(search);
        return new PageResponseDTO<>(content, page, size, total);
    }
}
