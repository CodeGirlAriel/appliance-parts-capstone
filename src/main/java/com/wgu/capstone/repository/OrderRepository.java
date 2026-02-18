package com.wgu.capstone.repository;

import com.wgu.capstone.entity.Order;
import com.wgu.capstone.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.part LEFT JOIN FETCH i.selectedSupplier s LEFT JOIN FETCH s.supplier WHERE o.status = :status")
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.part LEFT JOIN FETCH i.selectedSupplier s LEFT JOIN FETCH s.supplier WHERE o.status = :status AND o.isCartItem = :isCartItem")
    List<Order> findByStatusAndIsCartItem(OrderStatus status, Boolean isCartItem);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.part LEFT JOIN FETCH i.selectedSupplier s LEFT JOIN FETCH s.supplier WHERE o.orderId = :orderId")
    java.util.Optional<Order> findByIdWithRelations(Long orderId);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.part LEFT JOIN FETCH i.selectedSupplier s LEFT JOIN FETCH s.supplier ORDER BY o.createdAt DESC")
    List<Order> findAllWithRelations();
}

