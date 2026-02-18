package com.wgu.capstone.service.dto;

import java.util.List;

public class PartComparisonDto {

    private final String partId;
    private final String partName;
    private final List <SupplierOptionDto> options;

    public PartComparisonDto(String partId, String partName, List<SupplierOptionDto> options){
        this.partId = partId;
        this.partName = partName;
        this.options = options;
    }

    public String getPartId() { return partId;}
    public String getPartName() { return partName;}
    public List<SupplierOptionDto> getOptions() {return options;}
    }

