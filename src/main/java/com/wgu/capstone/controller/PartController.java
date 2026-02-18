package com.wgu.capstone.controller;

import com.wgu.capstone.entity.Part;
import com.wgu.capstone.service.PartSearchService;
import com.wgu.capstone.service.PartSearchService.SortMode;
import com.wgu.capstone.service.dto.PartComparisonDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parts")
@CrossOrigin(origins = "http://localhost:4200")
public class PartController {

    private final PartSearchService partSearchService;

    public PartController(PartSearchService partSearchService) {
        this.partSearchService = partSearchService;
    }

    @GetMapping("/search")
    public List<Part> searchParts(@RequestParam String query) {
        return partSearchService.searchParts(query);
    }

    @GetMapping("/{partId}/compare")
    public PartComparisonDto comparePartSuppliers(
            @PathVariable String partId,
            @RequestParam(required = false) SortMode sort
    ) {
        return partSearchService.getComparisonForPart(partId, sort);
    }
}