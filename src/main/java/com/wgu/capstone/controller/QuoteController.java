package com.wgu.capstone.controller;

import com.wgu.capstone.controller.dto.SaveQuoteRequest;
import com.wgu.capstone.controller.dto.UpdateQuoteRequest;
import com.wgu.capstone.entity.Order;
import com.wgu.capstone.entity.enums.OrderStatus;
import com.wgu.capstone.service.QuoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quotes")
@CrossOrigin(origins = "http://localhost:4200")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public Order saveQuote(@RequestBody SaveQuoteRequest request) {
        return quoteService.saveQuote(
                request.getPartSupplierId(),
                request.getQuantity(),
                request.getIsCartItem()
        );
    }

    @GetMapping
    public List<Order> getAllQuotes() {
        return quoteService.getAllQuotes();
    }
    
    @DeleteMapping("/{orderId}")
    public void deleteQuote(@PathVariable Long orderId) {
        quoteService.deleteQuote(orderId);
    }
    
    @PutMapping("/{orderId}/supplier")
    public Order updateQuoteSupplier(
            @PathVariable Long orderId,
            @RequestBody UpdateQuoteRequest request
    ) {
        return quoteService.updateQuoteSupplier(orderId, request.getNewPartSupplierId());
    }
}