package com.residence.repair.controller;

import com.residence.repair.dto.request.OrderScheduleRequest;
import com.residence.repair.dto.request.UpdateOrderStatusRequest;
import com.residence.repair.dto.response.ApiResponse;
import com.residence.repair.dto.response.OrderResponse;
import com.residence.repair.dto.response.OrderSummaryResponse;
import com.residence.repair.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * Voir tous les ordres de la résidence (Global).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders()));
    }

    /**
     * Obtenir détail d'une commande.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderDetails(id)));
    }

    /**
     * Planifier une intervention.
     */
    @PutMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<Void>> schedule(@PathVariable Long id, @Valid @RequestBody OrderScheduleRequest request) {
        orderService.scheduleOrder(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /**
     * Terminer la commande.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        orderService.completeOrder(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}