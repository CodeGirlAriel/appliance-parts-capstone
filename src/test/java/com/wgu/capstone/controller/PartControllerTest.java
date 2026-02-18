package com.wgu.capstone.controller;

import com.wgu.capstone.entity.Part;
import com.wgu.capstone.service.PartSearchService;
import com.wgu.capstone.service.PartSearchService.SortMode;
import com.wgu.capstone.service.dto.PartComparisonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartControllerTest {

    @Mock
    private PartSearchService partSearchService;

    @InjectMocks
    private PartController partController;

    private Part testPart1;
    private Part testPart2;
    private PartComparisonDto testComparison;

    @BeforeEach
    void setUp() {
        testPart1 = new Part("WPW10123456", "Washer Drain Pump");
        testPart2 = new Part("WPW10315885", "Washer Agitator");
        testComparison = new PartComparisonDto(
            "WPW10123456",
            "Washer Drain Pump",
            List.of()
        );
    }

    @Test
    void testSearchParts_Success() {
        // Given
        String query = "WPW";
        List<Part> expectedParts = Arrays.asList(testPart1, testPart2);
        when(partSearchService.searchParts(query)).thenReturn(expectedParts);

        // When
        List<Part> result = partController.searchParts(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedParts, result);
        verify(partSearchService).searchParts(query);
    }

    @Test
    void testSearchParts_EmptyResults() {
        // Given
        String query = "NONEXISTENT";
        when(partSearchService.searchParts(query)).thenReturn(List.of());

        // When
        List<Part> result = partController.searchParts(query);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(partSearchService).searchParts(query);
    }

    @Test
    void testComparePartSuppliers_WithoutSort() {
        // Given
        String partId = "WPW10123456";
        when(partSearchService.getComparisonForPart(partId, null))
            .thenReturn(testComparison);

        // When
        PartComparisonDto result = partController.comparePartSuppliers(partId, null);

        // Then
        assertNotNull(result);
        assertEquals(testComparison, result);
        assertEquals(partId, result.getPartId());
        verify(partSearchService).getComparisonForPart(partId, null);
    }

    @Test
    void testComparePartSuppliers_WithSort() {
        // Given
        String partId = "WPW10123456";
        SortMode sortMode = SortMode.CHEAPEST;
        when(partSearchService.getComparisonForPart(partId, sortMode))
            .thenReturn(testComparison);

        // When
        PartComparisonDto result = partController.comparePartSuppliers(partId, sortMode);

        // Then
        assertNotNull(result);
        assertEquals(testComparison, result);
        verify(partSearchService).getComparisonForPart(partId, sortMode);
    }

    @Test
    void testComparePartSuppliers_WithFastestShippingSort() {
        // Given
        String partId = "WPW10123456";
        SortMode sortMode = SortMode.FASTEST_SHIPPING;
        when(partSearchService.getComparisonForPart(partId, sortMode))
            .thenReturn(testComparison);

        // When
        PartComparisonDto result = partController.comparePartSuppliers(partId, sortMode);

        // Then
        assertNotNull(result);
        verify(partSearchService).getComparisonForPart(partId, sortMode);
    }

    @Test
    void testSearchParts_PropagatesException() {
        // Given
        String query = "";
        when(partSearchService.searchParts(query))
            .thenThrow(new IllegalArgumentException("Search query cannot be empty"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            partController.searchParts(query)
        );
        verify(partSearchService).searchParts(query);
    }

    @Test
    void testComparePartSuppliers_PropagatesException() {
        // Given
        String partId = "INVALID";
        when(partSearchService.getComparisonForPart(eq(partId), any()))
            .thenThrow(new IllegalArgumentException("Part not found: " + partId));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            partController.comparePartSuppliers(partId, null)
        );
        verify(partSearchService).getComparisonForPart(partId, null);
    }
}

