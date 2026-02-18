package com.wgu.capstone.service;

import com.wgu.capstone.entity.Order;
import com.wgu.capstone.entity.OrderItem;
import com.wgu.capstone.entity.PartSupplier;
import com.wgu.capstone.entity.enums.OrderStatus;
import com.wgu.capstone.repository.OrderRepository;
import com.wgu.capstone.repository.PartSupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Service
public class QuoteService {

    private final OrderRepository orderRepository;
    private final PartSupplierRepository partSupplierRepository;

    public QuoteService(
            OrderRepository orderRepository,
            PartSupplierRepository partSupplierRepository
    ) {
        this.orderRepository = orderRepository;
        this.partSupplierRepository = partSupplierRepository;
    }

    @Transactional
    public Order saveQuote(Long partSupplierId, Integer quantity, Boolean isCartItem) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        PartSupplier partSupplier = partSupplierRepository.findByIdWithRelations(partSupplierId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid part supplier ID: " + partSupplierId)
                );

        // Ensure Part is loaded
        if (partSupplier.getPart() == null) {
            throw new IllegalStateException("Part information is missing for part supplier: " + partSupplierId);
        }

        // Validate stock before proceeding
        if (partSupplier.getNumInStock() == null || partSupplier.getNumInStock() < quantity) {
            throw new IllegalStateException(
                    "Not enough stock. Available: " + 
                    (partSupplier.getNumInStock() != null ? partSupplier.getNumInStock() : 0) + 
                    ", requested: " + quantity
            );
        }

        // Create a new order (QUOTE)
        Order order = new Order();
        order.setStatus(OrderStatus.QUOTE);
        order.setIsCartItem(isCartItem != null ? isCartItem : false);

        // Lock price at time of quote
        BigDecimal unitPrice = partSupplier.getPartCost();

        OrderItem item = new OrderItem(
                order,
                partSupplier.getPart(),
                quantity,
                unitPrice
        );
        
        // Set the selected supplier for this order item
        item.setSelectedSupplier(partSupplier);

        // Maintain bidirectional relationship
        order.addItem(item);

        // Reserve inventory (this modifies numInStock)
        partSupplier.calculateStock(quantity);
        
        // Save the updated PartSupplier with new stock level
        partSupplierRepository.save(partSupplier);

        // Recalculate totals
        order.recalculateTotals();

        return orderRepository.save(order);
    }

    @GetMapping
    public List<Order> getAllQuotes() {
        // Return only saved quotes (not cart items)
        return orderRepository.findByStatusAndIsCartItem(OrderStatus.QUOTE, false);
    }

    public List<Order> getCartItems() {
        // Return only cart items
        return orderRepository.findByStatusAndIsCartItem(OrderStatus.QUOTE, true);
    }
    
    @Transactional
    public void deleteQuote(Long orderId) {
        // Fetch order with items and their selected suppliers
        Order order = orderRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + orderId));
        
        if (order.getStatus() != OrderStatus.QUOTE) {
            throw new IllegalStateException("Only quotes can be deleted. Order status: " + order.getStatus());
        }
        
        // Restore stock for each item in the order
        for (OrderItem item : order.getItems()) {
            if (item.getSelectedSupplier() != null) {
                Long partSupplierId = item.getSelectedSupplier().getPartSupplierId();
                PartSupplier partSupplier = partSupplierRepository.findByIdWithRelations(partSupplierId)
                        .orElse(null);
                
                if (partSupplier != null) {
                    // Restore the stock that was reserved
                    partSupplier.restock(item.getQuantity());
                    partSupplierRepository.save(partSupplier);
                }
            }
        }
        
        // Delete the order (cascade will delete order items)
        orderRepository.delete(order);
    }
    
    @Transactional
    public Order updateQuoteSupplier(Long orderId, Long newPartSupplierId) {
        // Fetch order with items and their selected suppliers
        Order order = orderRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + orderId));
        
        if (order.getStatus() != OrderStatus.QUOTE) {
            throw new IllegalStateException("Only quotes can be updated. Order status: " + order.getStatus());
        }
        
        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Quote has no items to update");
        }

        OrderItem item = order.getItems().get(0);
        
        // Fetch the new PartSupplier
        PartSupplier newPartSupplier = partSupplierRepository.findByIdWithRelations(newPartSupplierId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid part supplier ID: " + newPartSupplierId));
        
        // Validate that the new supplier has the same part
        if (!newPartSupplier.getPart().getPartId().equals(item.getPart().getPartId())) {
            throw new IllegalStateException(
                    "Cannot change part. Quote is for part " + item.getPart().getPartId() + 
                    ", but new supplier is for part " + newPartSupplier.getPart().getPartId()
            );
        }
        
        // Validate stock availability
        if (newPartSupplier.getNumInStock() == null || newPartSupplier.getNumInStock() < item.getQuantity()) {
            throw new IllegalStateException(
                    "Not enough stock. Available: " + 
                    (newPartSupplier.getNumInStock() != null ? newPartSupplier.getNumInStock() : 0) + 
                    ", requested: " + item.getQuantity()
            );
        }
        
        // Restore stock from old supplier
        if (item.getSelectedSupplier() != null) {
            Long oldPartSupplierId = item.getSelectedSupplier().getPartSupplierId();
            PartSupplier oldPartSupplier = partSupplierRepository.findByIdWithRelations(oldPartSupplierId)
                    .orElse(null);
            
            if (oldPartSupplier != null) {
                oldPartSupplier.restock(item.getQuantity());
                partSupplierRepository.save(oldPartSupplier);
            }
        }
        
        // Reserve stock from new supplier
        newPartSupplier.calculateStock(item.getQuantity());
        partSupplierRepository.save(newPartSupplier);
        
        // Update the order item with new supplier and price
        item.setSelectedSupplier(newPartSupplier);
        item.setUnitPrice(newPartSupplier.getPartCost());
        
        // Recalculate totals
        order.recalculateTotals();
        
        return orderRepository.save(order);
    }
}