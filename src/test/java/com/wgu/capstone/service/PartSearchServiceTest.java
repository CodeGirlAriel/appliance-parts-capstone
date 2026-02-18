package com.wgu.capstone.service;

import com.wgu.capstone.entity.Part;
import com.wgu.capstone.entity.PartSupplier;
import com.wgu.capstone.entity.Supplier;
import com.wgu.capstone.repository.PartRepository;
import com.wgu.capstone.repository.PartSupplierRepository;
import com.wgu.capstone.service.dto.PartComparisonDto;
import com.wgu.capstone.service.dto.SupplierOptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartSearchServiceTest {

    @Mock
    private PartRepository partRepository;

    @Mock
    private PartSupplierRepository partSupplierRepository;

    @InjectMocks
    private PartSearchService partSearchService;

    private Part testPart;
    private Supplier testSupplier;
    private PartSupplier testPartSupplier;

    @BeforeEach
    void setUp() {
        testPart = new Part("WPW10123456", "Washer Drain Pump");
        testSupplier = new Supplier("AppliancePartsPros", 3);
        testPartSupplier = new PartSupplier(testSupplier, testPart, new BigDecimal("50.00"), 12);
    }

    @Test
    void testSearchParts_ExactMatch() {
        // Given
        String query = "WPW10123456";
        when(partRepository.findById(query)).thenReturn(Optional.of(testPart));

        // When
        List<Part> result = partSearchService.searchParts(query);

        // Then
        assertEquals(1, result.size());
        assertEquals(testPart, result.get(0));
        verify(partRepository).findById(query);
        verify(partRepository, never()).findByPartIdContainingIgnoreCase(anyString());
        verify(partRepository, never()).findByPartNameContainingIgnoreCase(anyString());
    }

    @Test
    void testSearchParts_PartialMatchById() {
        // Given
        String query = "WPW";
        Part part1 = new Part("WPW10123456", "Washer Drain Pump");
        Part part2 = new Part("WPW10315885", "Washer Agitator");
        
        when(partRepository.findById(query)).thenReturn(Optional.empty());
        when(partRepository.findByPartIdContainingIgnoreCase(query)).thenReturn(Arrays.asList(part1, part2));
        when(partRepository.findByPartNameContainingIgnoreCase(query)).thenReturn(List.of());

        // When
        List<Part> result = partSearchService.searchParts(query);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(part1));
        assertTrue(result.contains(part2));
    }

    @Test
    void testSearchParts_PartialMatchByName() {
        // Given
        String query = "Washer";
        Part part1 = new Part("WPW10123456", "Washer Drain Pump");
        Part part2 = new Part("WPW10315885", "Washer Agitator");
        
        when(partRepository.findById(query)).thenReturn(Optional.empty());
        when(partRepository.findByPartIdContainingIgnoreCase(query)).thenReturn(List.of());
        when(partRepository.findByPartNameContainingIgnoreCase(query)).thenReturn(Arrays.asList(part1, part2));

        // When
        List<Part> result = partSearchService.searchParts(query);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(part1));
        assertTrue(result.contains(part2));
    }

    @Test
    void testSearchParts_CombinedResults_NoDuplicates() {
        // Given
        String query = "WPW";
        Part part1 = new Part("WPW10123456", "Washer Drain Pump");
        
        when(partRepository.findById(query)).thenReturn(Optional.empty());
        when(partRepository.findByPartIdContainingIgnoreCase(query)).thenReturn(List.of(part1));
        when(partRepository.findByPartNameContainingIgnoreCase(query)).thenReturn(List.of(part1));

        // When
        List<Part> result = partSearchService.searchParts(query);

        // Then
        assertEquals(1, result.size()); // Should not have duplicates
        assertEquals(part1, result.get(0));
    }

    @Test
    void testSearchParts_EmptyQuery() {
        // Given
        String query = "";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> partSearchService.searchParts(query));
        verify(partRepository, never()).findById(anyString());
    }

    @Test
    void testSearchParts_NullQuery() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> partSearchService.searchParts(null));
        verify(partRepository, never()).findById(anyString());
    }

    @Test
    void testSearchParts_WhitespaceQuery() {
        // Given
        String query = "   ";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> partSearchService.searchParts(query));
        verify(partRepository, never()).findById(anyString());
    }

    @Test
    void testGetComparisonForPart_Success() {
        // Given
        String partId = "WPW10123456";
        PartSupplier ps1 = new PartSupplier(testSupplier, testPart, new BigDecimal("50.00"), 12);
        PartSupplier ps2 = new PartSupplier(
            new Supplier("RepairClinic", 5), 
            testPart, 
            new BigDecimal("55.00"), 
            8
        );

        when(partRepository.findById(partId)).thenReturn(Optional.of(testPart));
        when(partSupplierRepository.findByPart_PartId(partId)).thenReturn(Arrays.asList(ps1, ps2));

        // When
        PartComparisonDto result = partSearchService.getComparisonForPart(partId, null);

        // Then
        assertNotNull(result);
        assertEquals(partId, result.getPartId());
        assertEquals("Washer Drain Pump", result.getPartName());
        assertEquals(2, result.getOptions().size());
    }

    @Test
    void testGetComparisonForPart_PartNotFound() {
        // Given
        String partId = "INVALID";
        when(partRepository.findById(partId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            partSearchService.getComparisonForPart(partId, null)
        );
    }

    @Test
    void testGetComparisonForPart_SortedByCheapest() {
        // Given
        String partId = "WPW10123456";
        PartSupplier ps1 = new PartSupplier(testSupplier, testPart, new BigDecimal("55.00"), 12);
        PartSupplier ps2 = new PartSupplier(
            new Supplier("RepairClinic", 5), 
            testPart, 
            new BigDecimal("50.00"), 
            8
        );

        when(partRepository.findById(partId)).thenReturn(Optional.of(testPart));
        when(partSupplierRepository.findByPart_PartId(partId)).thenReturn(Arrays.asList(ps1, ps2));

        // When
        PartComparisonDto result = partSearchService.getComparisonForPart(
            partId, 
            PartSearchService.SortMode.CHEAPEST
        );

        // Then
        List<SupplierOptionDto> options = result.getOptions();
        assertEquals(2, options.size());
        assertEquals(new BigDecimal("50.00"), options.get(0).getPartCost()); // Cheapest first
        assertEquals(new BigDecimal("55.00"), options.get(1).getPartCost());
    }

    @Test
    void testGetComparisonForPart_SortedByFastestShipping() {
        // Given
        String partId = "WPW10123456";
        PartSupplier ps1 = new PartSupplier(
            new Supplier("SlowSupplier", 7), 
            testPart, 
            new BigDecimal("50.00"), 
            12
        );
        PartSupplier ps2 = new PartSupplier(
            new Supplier("FastSupplier", 2), 
            testPart, 
            new BigDecimal("55.00"), 
            8
        );

        when(partRepository.findById(partId)).thenReturn(Optional.of(testPart));
        when(partSupplierRepository.findByPart_PartId(partId)).thenReturn(Arrays.asList(ps1, ps2));

        // When
        PartComparisonDto result = partSearchService.getComparisonForPart(
            partId, 
            PartSearchService.SortMode.FASTEST_SHIPPING
        );

        // Then
        List<SupplierOptionDto> options = result.getOptions();
        assertEquals(2, options.size());
        assertEquals(2, options.get(0).getShippingTime()); // Fastest first
        assertEquals(7, options.get(1).getShippingTime());
    }

    @Test
    void testGetComparisonForPart_NoSortMode() {
        // Given
        String partId = "WPW10123456";
        PartSupplier ps1 = new PartSupplier(testSupplier, testPart, new BigDecimal("50.00"), 12);

        when(partRepository.findById(partId)).thenReturn(Optional.of(testPart));
        when(partSupplierRepository.findByPart_PartId(partId)).thenReturn(List.of(ps1));

        // When
        PartComparisonDto result = partSearchService.getComparisonForPart(partId, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getOptions().size());
    }
}

