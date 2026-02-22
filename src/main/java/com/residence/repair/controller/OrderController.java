package com.residence.repair.controller;

import com.residence.repair.dto.request.CreateOrderRequest;
import com.residence.repair.dto.response.OrderResponse;
import com.residence.repair.dto.response.OrderSummaryResponse;
import com.residence.repair.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour les actions des locataires sur leurs ordres.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT')") // Restriction au rôle Locataire
public class OrderController {

    private final OrderService orderService;

    /**
     * Créer une nouvelle demande de réparation.
     */
    @PostMapping
    public ResponseEntity<Void> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        orderService.createOrder(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Consulter ses propres ordres de réparation.
     */
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * Obtenir détail d'une commande.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetails(id));
    }

    /**
     * Annuler une commande avant planification.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}