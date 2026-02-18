package com.wgu.capstone.controller;

import com.wgu.capstone.entity.Order;
import com.wgu.capstone.entity.enums.OrderStatus;
import com.wgu.capstone.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{orderId}/checkout")
    public Order checkout(@PathVariable Long orderId) {
        return orderService.checkout(orderId);
    }

    @PostMapping("/{orderId}/process")
    public Order processOrder(@PathVariable Long orderId) {
        return orderService.processOrder(orderId);
    }

    @PostMapping("/{orderId}/complete")
    public Order completeOrder(@PathVariable Long orderId) {
        return orderService.completeOrder(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public Order cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderService.getOrdersByStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }
}

