package com.residence.repair.controller;

import com.residence.repair.dto.request.OrderScheduleRequest;
import com.residence.repair.dto.request.UpdateOrderStatusRequest;
import com.residence.repair.dto.response.OrderResponse;
import com.residence.repair.dto.response.OrderSummaryResponse;
import com.residence.repair.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion administrative des ordres.
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Restriction au rôle Admin
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * Voir tous les ordres de la résidence (Global).
     */
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getAllOrders() {
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
     * Planifier une intervention.
     */
    @PutMapping("/{id}/schedule")
    public ResponseEntity<Void> scheduleOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderScheduleRequest request) {
        orderService.scheduleOrder(id, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Mettre à jour le statut (ex: Marquer comme Terminé).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        orderService.updateStatus(id, request);
        return ResponseEntity.ok().build();
    }
}