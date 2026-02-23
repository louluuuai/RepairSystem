package com.residence.repair.controller;

import com.residence.repair.domain.enums.OrderStatus;
import com.residence.repair.dto.request.CreateOrderRequest;
import com.residence.repair.dto.response.ApiResponse;
import com.residence.repair.dto.response.OrderResponse;
import com.residence.repair.dto.response.OrderSummaryResponse;
import com.residence.repair.dto.response.PageResponse;
import com.residence.repair.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    /**
     * Créer une nouvelle demande de réparation.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    /**
     * Consulter ses propres ordres de réparation.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            OrderStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders(page, size, sort, direction, status)));
    }

    /**
     * Obtenir détail d'une commande.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderDetails(id)));
    }

    /**
     * Annuler une commande avant planification.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}