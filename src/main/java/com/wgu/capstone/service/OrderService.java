package com.wgu.capstone.service;

import com.wgu.capstone.entity.Order;
import com.wgu.capstone.entity.enums.OrderStatus;
import com.wgu.capstone.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order checkout(Long orderId) {
        Order order = orderRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.QUOTE) {
            throw new IllegalStateException("Only quotes can be checked out. Current status: " + order.getStatus());
        }

        // Change status from QUOTE to NEW (ready for processing)
        order.setStatus(OrderStatus.NEW);
        order.recalculateTotals();

        return orderRepository.save(order);
    }

    @Transactional
    public Order processOrder(Long orderId) {
        Order order = orderRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Only NEW orders can be processed. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PROCESSING);
        order.recalculateTotals();

        return orderRepository.save(order);
    }

    @Transactional
    public Order completeOrder(Long orderId) {
        Order order = orderRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Only PROCESSING orders can be completed. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.recalculateTotals();

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be canceled.");
        }

        // Restore stock if order was in QUOTE or NEW status
        if (order.getStatus() == OrderStatus.QUOTE || order.getStatus() == OrderStatus.NEW) {

        }

        order.setStatus(OrderStatus.CANCELED);
        order.recalculateTotals();

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllWithRelations();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
}

