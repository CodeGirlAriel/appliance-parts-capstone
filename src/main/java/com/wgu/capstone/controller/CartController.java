package com.wgu.capstone.controller;

import com.wgu.capstone.entity.Order;
import com.wgu.capstone.service.QuoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final QuoteService quoteService;

    public CartController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping
    public List<Order> getCartItems() {
        return quoteService.getCartItems();
    }
}

