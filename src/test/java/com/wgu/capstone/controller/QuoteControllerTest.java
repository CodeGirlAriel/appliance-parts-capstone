package com.wgu.capstone.controller;

import com.wgu.capstone.controller.dto.SaveQuoteRequest;
import com.wgu.capstone.controller.dto.UpdateQuoteRequest;
import com.wgu.capstone.entity.Order;
import com.wgu.capstone.entity.enums.OrderStatus;
import com.wgu.capstone.service.QuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteControllerTest {

    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private QuoteController quoteController;

    private Order testOrder;
    private SaveQuoteRequest saveRequest;
    private UpdateQuoteRequest updateRequest;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setStatus(OrderStatus.QUOTE);
        
        saveRequest = new SaveQuoteRequest();
        saveRequest.setPartSupplierId(1L);
        saveRequest.setQuantity(2);
        
        updateRequest = new UpdateQuoteRequest();
        updateRequest.setNewPartSupplierId(2L);
    }

    @Test
    void testSaveQuote_Success() {
        // Given
        when(quoteService.saveQuote(1L, 2, false)).thenReturn(testOrder);

        // When
        Order result = quoteController.saveQuote(saveRequest);

        // Then
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(quoteService).saveQuote(1L, 2, false);
    }

    @Test
    void testSaveQuote_WithDifferentValues() {
        // Given
        saveRequest.setPartSupplierId(5L);
        saveRequest.setQuantity(10);
        when(quoteService.saveQuote(5L, 10, false)).thenReturn(testOrder);

        // When
        Order result = quoteController.saveQuote(saveRequest);

        // Then
        assertNotNull(result);
        verify(quoteService).saveQuote(5L, 10, false);
    }

    @Test
    void testGetAllQuotes_Success() {
        // Given
        List<Order> quotes = Arrays.asList(testOrder);
        when(quoteService.getAllQuotes()).thenReturn(quotes);

        // When
        List<Order> result = quoteController.getAllQuotes();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(quoteService).getAllQuotes();
    }

    @Test
    void testGetAllQuotes_EmptyList() {
        // Given
        when(quoteService.getAllQuotes()).thenReturn(List.of());

        // When
        List<Order> result = quoteController.getAllQuotes();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(quoteService).getAllQuotes();
    }

    @Test
    void testDeleteQuote_Success() {
        // Given
        Long orderId = 1L;
        doNothing().when(quoteService).deleteQuote(orderId);

        // When
        quoteController.deleteQuote(orderId);

        // Then
        verify(quoteService).deleteQuote(orderId);
    }

    @Test
    void testDeleteQuote_WithDifferentId() {
        // Given
        Long orderId = 999L;
        doNothing().when(quoteService).deleteQuote(orderId);

        // When
        quoteController.deleteQuote(orderId);

        // Then
        verify(quoteService).deleteQuote(orderId);
    }

    @Test
    void testUpdateQuoteSupplier_Success() {
        // Given
        Long orderId = 1L;
        Long newPartSupplierId = 2L;
        when(quoteService.updateQuoteSupplier(orderId, newPartSupplierId))
            .thenReturn(testOrder);

        // When
        Order result = quoteController.updateQuoteSupplier(orderId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(quoteService).updateQuoteSupplier(orderId, newPartSupplierId);
    }

    @Test
    void testUpdateQuoteSupplier_WithDifferentValues() {
        // Given
        Long orderId = 5L;
        Long newPartSupplierId = 10L;
        updateRequest.setNewPartSupplierId(newPartSupplierId);
        when(quoteService.updateQuoteSupplier(orderId, newPartSupplierId))
            .thenReturn(testOrder);

        // When
        Order result = quoteController.updateQuoteSupplier(orderId, updateRequest);

        // Then
        assertNotNull(result);
        verify(quoteService).updateQuoteSupplier(orderId, newPartSupplierId);
    }

    @Test
    void testSaveQuote_PropagatesException() {
        // Given
        when(quoteService.saveQuote(anyLong(), anyInt(), anyBoolean()))
            .thenThrow(new IllegalArgumentException("Invalid quantity"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            quoteController.saveQuote(saveRequest)
        );
        verify(quoteService).saveQuote(1L, 2, false);
    }

    @Test
    void testDeleteQuote_PropagatesException() {
        // Given
        Long orderId = 999L;
        doThrow(new IllegalArgumentException("Quote not found"))
            .when(quoteService).deleteQuote(orderId);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            quoteController.deleteQuote(orderId)
        );
        verify(quoteService).deleteQuote(orderId);
    }

    @Test
    void testUpdateQuoteSupplier_PropagatesException() {
        // Given
        Long orderId = 1L;
        when(quoteService.updateQuoteSupplier(anyLong(), anyLong()))
            .thenThrow(new IllegalStateException("Not enough stock"));

        // When/Then
        assertThrows(IllegalStateException.class, () -> 
            quoteController.updateQuoteSupplier(orderId, updateRequest)
        );
        verify(quoteService).updateQuoteSupplier(orderId, 2L);
    }
}

