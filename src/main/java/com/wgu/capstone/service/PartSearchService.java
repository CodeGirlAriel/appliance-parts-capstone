package com.wgu.capstone.service;

import com.wgu.capstone.entity.Part;
import com.wgu.capstone.entity.PartSupplier;
import com.wgu.capstone.repository.PartRepository;
import com.wgu.capstone.repository.PartSupplierRepository;
import com.wgu.capstone.service.dto.PartComparisonDto;
import com.wgu.capstone.service.dto.SupplierOptionDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PartSearchService {

    private final PartRepository partRepository;
    private final PartSupplierRepository partSupplierRepository;

    public PartSearchService(PartRepository partRepository, PartSupplierRepository partSupplierRepository) {
        this.partRepository = partRepository;
        this.partSupplierRepository = partSupplierRepository;
    }

    public List<Part> searchParts(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        String q = query.trim();

        // First try exact match by ID
        Optional<Part> exactPart = partRepository.findById(q);
        if (exactPart.isPresent()) {
            return List.of(exactPart.get());
        }

        // Search by part ID (partial match) and part name (partial match)
        List<Part> byId = partRepository.findByPartIdContainingIgnoreCase(q);
        List<Part> byName = partRepository.findByPartNameContainingIgnoreCase(q);
        
        // Combine results and remove duplicates (using partId as unique identifier)
        java.util.Map<String, Part> uniqueParts = new java.util.LinkedHashMap<>();
        byId.forEach(part -> uniqueParts.putIfAbsent(part.getPartId(), part));
        byName.forEach(part -> uniqueParts.putIfAbsent(part.getPartId(), part));
        
        return new java.util.ArrayList<>(uniqueParts.values());
    }

    public PartComparisonDto getComparisonForPart(String partId, SortMode sortMode) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found: " + partId));

        List<PartSupplier> vendorRows = partSupplierRepository.findByPart_PartId(partId);

        List<SupplierOptionDto> options = vendorRows.stream()
                .map(ps -> new SupplierOptionDto(
                        ps.getPartSupplierId(),
                        ps.getSupplierId(),
                        ps.getSupplier().getSupplierName(),
                        ps.getPartCost(),
                        ps.getNumInStock(),
                        ps.getSupplier().getShippingTime()
                ))
                .toList();

        List<SupplierOptionDto> sortedOptions = sortOptions(options, sortMode);

        return new PartComparisonDto(part.getPartId(), part.getPartName(), sortedOptions);
    }

    private List<SupplierOptionDto> sortOptions(List<SupplierOptionDto> options, SortMode sortMode) {
        if (sortMode == null) {
            return options;
        }

        return switch (sortMode) {
            case CHEAPEST -> options.stream()
                    .sorted(Comparator.comparing(SupplierOptionDto::getPartCost))
                    .toList();

            case FASTEST_SHIPPING -> options.stream()
                    .sorted(Comparator.comparing(SupplierOptionDto::getShippingTime))
                    .toList();
        };
    }

    public enum SortMode {
        CHEAPEST,
        FASTEST_SHIPPING
    }
}
