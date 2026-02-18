package com.wgu.capstone.service;

import com.wgu.capstone.entity.*;
import com.wgu.capstone.entity.enums.OrderStatus;
import com.wgu.capstone.repository.OrderRepository;
import com.wgu.capstone.repository.PartSupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PartSupplierRepository partSupplierRepository;

    @InjectMocks
    private QuoteService quoteService;

    private Part testPart;
    private Supplier testSupplier;
    private PartSupplier testPartSupplier;
    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testPart = new Part("WPW10123456", "Washer Drain Pump");
        testSupplier = new Supplier("AppliancePartsPros", 3);
        testPartSupplier = new PartSupplier(testSupplier, testPart, new BigDecimal("50.00"), 12);
        testOrder = new Order();
        testOrder.setStatus(OrderStatus.QUOTE);
        testOrderItem = new OrderItem(testOrder, testPart, 2, new BigDecimal("50.00"));
        testOrderItem.setSelectedSupplier(testPartSupplier);
        testOrder.addItem(testOrderItem);
        
        // Simulate that stock was already reduced when the quote was created
        // Initial stock: 12, quantity: 2, so stock should be 10 after quote creation
        testPartSupplier.calculateStock(2); // Reduce stock from 12 to 10
    }

    @Test
    void testSaveQuote_Success() {
        // Given
        Long partSupplierId = 1L;
        Integer quantity = 2;
        
        // Reset stock to 12 for this test since we're creating a NEW quote
        // (setUp() reduced it to 10 for other tests that use existing testOrder)
        testPartSupplier.setNumInStock(12);
        
        when(partSupplierRepository.findByIdWithRelations(partSupplierId))
            .thenReturn(Optional.of(testPartSupplier));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return order;
        });
        when(partSupplierRepository.save(any(PartSupplier.class))).thenAnswer(invocation -> {
            PartSupplier ps = invocation.getArgument(0);
            return ps;
        });

        // When
        Order result = quoteService.saveQuote(partSupplierId, quantity, false);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.QUOTE, result.getStatus());
        assertEquals(false, result.getIsCartItem());
        assertEquals(1, result.getItems().size());
        assertEquals(quantity, result.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("50.00"), result.getItems().get(0).getUnitPrice());
        assertEquals(10, testPartSupplier.getNumInStock()); // 12 - 2 = 10
        
        verify(partSupplierRepository).findByIdWithRelations(partSupplierId);
        verify(partSupplierRepository).save(testPartSupplier);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testSaveQuote_InvalidQuantity_Zero() {
        // Given
        Long partSupplierId = 1L;
        Integer quantity = 0;

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            quoteService.saveQuote(partSupplierId, quantity, false)
        );
        verify(partSupplierRepository, never()).findByIdWithRelations(any());
    }

    @Test
    void testSaveQuote_InvalidQuantity_Negative() {
        // Given
        Long partSupplierId = 1L;
        Integer quantity = -1;

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            quoteService.saveQuote(partSupplierId, quantity, false)
        );
        verify(partSupplierRepository, never()).findByIdWithRelations(any());
    }

    @Test
    void testSaveQuote_InvalidQuantity_Null() {
        // Given
        Long partSupplierId = 1L;
        Integer quantity = null;

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            quoteService.saveQuote(partSupplierId, quantity, false)
        );
        verify(partSupplierRepository, never()).findByIdWithRelations(any());
    }

    @Test
    void testSaveQuote_InvalidPartSupplierId() {
        // Given
        Long partSupplierId = 999L;
        Integer quantity = 2;
        
        when(partSupplierRepository.findByIdWithRelations(partSupplierId))
            .thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            quoteService.saveQuote(partSupplierId, quantity, false)
        );
    }

    @Test
    void testSaveQuote_InsufficientStock() {
        // Given
        Long partSupplierId = 1L;
        Integer quantity = 20; // More than available (12)
        
        when(partSupplierRepository.findByIdWithRelations(partSupplierId))
            .thenReturn(Optional.of(testPartSupplier));

        // When/Then
        assertThrows(IllegalStateException.class, () -> 
            quoteService.saveQuote(partSupplierId, quantity, false)
        );
        verify(partSupplierRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testSaveQuote_AsCartItem() {
        // Given
        Long partSupplierId = 1L;
        Integer quantity = 2;
        
        when(partSupplierRepository.findByIdWithRelations(partSupplierId))
            .thenReturn(Optional.of(testPartSupplier));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return order;
        });
        when(partSupplierRepository.save(any(PartSupplier.class))).thenAnswer(invocation -> {
            PartSupplier ps = invocation.getArgument(0);
            return ps;
        });

        // When
        Order result = quoteService.saveQuote(partSupplierId, quantity, true);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.QUOTE, result.getStatus());
        assertEquals(true, result.getIsCartItem());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testSaveQuote_AsSavedQuote() {
        // Given
        Long partSupplierId = 1L;
        Integer quantity = 2;
        
        when(partSupplierRepository.findByIdWithRelations(partSupplierId))
            .thenReturn(Optional.of(testPartSupplier));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return order;
        });
        when(partSupplierRepository.save(any(PartSupplier.class))).thenAnswer(invocation -> {
            PartSupplier ps = invocation.getArgument(0);
            return ps;
        });

        // When
        Order result = quoteService.saveQuote(partSupplierId, quantity, false);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.QUOTE, result.getStatus());
        assertEquals(false, result.getIsCartItem());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testGetAllQuotes() {
        // Given
        List<Order> quotes = Arrays.asList(testOrder);
        when(orderRepository.findByStatusAndIsCartItem(OrderStatus.QUOTE, false)).thenReturn(quotes);

        // When
        List<Order> result = quoteService.getAllQuotes();

        // Then
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderRepository).findByStatusAndIsCartItem(OrderStatus.QUOTE, false);
    }

    @Test
    void testDeleteQuote_Success() {
        // Given
        Long orderId = 1L;
        Long partSupplierId = 1L; // Set an ID for the test PartSupplier
        
        // Use reflection to set the partSupplierId since there's no setter
        try {
            java.lang.reflect.Field idField = PartSupplier.class.getDeclaredField("partSupplierId");
            idField.setAccessible(true);
            idField.set(testPartSupplier, partSupplierId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set partSupplierId for test", e);
        }
        
        when(orderRepository.findByIdWithRelations(orderId))
            .thenReturn(Optional.of(testOrder));
        when(partSupplierRepository.findByIdWithRelations(partSupplierId))
            .thenReturn(Optional.of(testPartSupplier));
        when(partSupplierRepository.save(any(PartSupplier.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        quoteService.deleteQuote(orderId);

        // Then
        assertEquals(12, testPartSupplier.getNumInStock()); // Stock restored
        verify(partSupplierRepository).save(testPartSupplier);
        verify(orderRepository).delete(testOrder);
    }

    @Test
    void testDeleteQuote_NotFound() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findByIdWithRelations(orderId))
            .thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            quoteService.deleteQuote(orderId)
        );
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void testDeleteQuote_NotAQuote() {
        // Given
        Long orderId = 1L;
        testOrder.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findByIdWithRelations(orderId))
            .thenReturn(Optional.of(testOrder));

        // When/Then
        assertThrows(IllegalStateException.class, () -> 
            quoteService.deleteQuote(orderId)
        );
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void testUpdateQuoteSupplier_Success() {
        // Given
        Long orderId = 1L;
        Long oldPartSupplierId = 1L;
        Long newPartSupplierId = 2L;
        PartSupplier newPartSupplier = new PartSupplier(
            new Supplier("RepairClinic", 5),
            testPart,
            new BigDecimal("55.00"),
            10
        );
        
        // Set IDs for both suppliers
        try {
            java.lang.reflect.Field idField = PartSupplier.class.getDeclaredField("partSupplierId");
            idField.setAccessible(true);
            idField.set(testPartSupplier, oldPartSupplierId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set partSupplierId for test", e);
        }

        when(orderRepository.findByIdWithRelations(orderId))
            .thenReturn(Optional.of(testOrder));
        when(partSupplierRepository.findByIdWithRelations(newPartSupplierId))
            .thenReturn(Optional.of(newPartSupplier));
        when(partSupplierRepository.findByIdWithRelations(oldPartSupplierId))
            .thenReturn(Optional.of(testPartSupplier));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> 
            invocation.getArgument(0)
        );
        when(partSupplierRepository.save(any(PartSupplier.class))).thenAnswer(invocation -> 
            invocation.getArgument(0)
        );

        // When
        Order result = quoteService.updateQuoteSupplier(orderId, newPartSupplierId);

        // Then
        assertNotNull(result);
        assertEquals(newPartSupplier, testOrderItem.getSelectedSupplier());
        assertEquals(new BigDecimal("55.00"), testOrderItem.getUnitPrice());
        assertEquals(12, testPartSupplier.getNumInStock()); // Old stock restored (10 + 2 = 12)
        assertEquals(8, newPartSupplier.getNumInStock()); // New stock reduced (10 - 2 = 8)
        
        verify(partSupplierRepository).save(testPartSupplier);
        verify(partSupplierRepository).save(newPartSupplier);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void testUpdateQuoteSupplier_QuoteNotFound() {
        // Given
        Long orderId = 999L;
        Long newPartSupplierId = 2L;
        
        when(orderRepository.findByIdWithRelations(orderId))
            .thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            quoteService.updateQuoteSupplier(orderId, newPartSupplierId)
        );
    }

    @Test
    void testUpdateQuoteSupplier_NotAQuote() {
        // Given
        Long orderId = 1L;
        Long newPartSupplierId = 2L;
        testOrder.setStatus(OrderStatus.PROCESSING);
        
        when(orderRepository.findByIdWithRelations(orderId))
            .thenReturn(Optional.of(testOrder));

        // When/Then
        assertThrows(IllegalStateException.class, () -> 
            quoteService.updateQuoteSupplier(orderId, newPartSupplierId)
        );
    }

    @Test
    void testUpdateQuoteSupplier_DifferentPart() {
        // Given
        Long orderId = 1L;
        Long newPartSupplierId = 2L;
        Part differentPart = new Part("WPW10315885", "Washer Agitator");
        PartSupplier newPartSupplier = new PartSupplier(
            new Supplier("RepairClinic", 5),
            differentPart,
            new BigDecimal("55.00"),
            10
        );

        when(orderRepository.findByIdWithRelations(orderId))
            .thenReturn(Optional.of(testOrder));
        when(partSupplierRepository.findByIdWithRelations(newPartSupplierId))
            .thenReturn(Optional.of(newPartSupplier));

        // When/Then
        assertThrows(IllegalStateException.class, () -> 
            quoteService.updateQuoteSupplier(orderId, newPartSupplierId)
        );
    }

    @Test
    void testUpdateQuoteSupplier_InsufficientStock() {
        // Given
        Long orderId = 1L;
        Long newPartSupplierId = 2L;
        PartSupplier newPartSupplier = new PartSupplier(
            new Supplier("RepairClinic", 5),
            testPart,
            new BigDecimal("55.00"),
            1 // Only 1 in stock, but need 2
        );

        when(orderRepository.findByIdWithRelations(orderId))
            .thenReturn(Optional.of(testOrder));
        when(partSupplierRepository.findByIdWithRelations(newPartSupplierId))
            .thenReturn(Optional.of(newPartSupplier));

        // When/Then
        assertThrows(IllegalStateException.class, () -> 
            quoteService.updateQuoteSupplier(orderId, newPartSupplierId)
        );
    }

    @Test
    void testUpdateQuoteSupplier_NoItems() {
        // Given
        Long orderId = 1L;
        Long newPartSupplierId = 2L;
        Order emptyOrder = new Order();
        emptyOrder.setStatus(OrderStatus.QUOTE);
        // No items added

        when(orderRepository.findByIdWithRelations(orderId))
            .thenReturn(Optional.of(emptyOrder));

        // When/Then
        assertThrows(IllegalStateException.class, () -> 
            quoteService.updateQuoteSupplier(orderId, newPartSupplierId)
        );
    }
}

