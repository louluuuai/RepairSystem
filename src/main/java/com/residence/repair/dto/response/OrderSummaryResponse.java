package com.residence.repair.dto.response;

import com.residence.repair.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Résumé d'une commande pour affichage liste.
 */
@Data
@Builder
public class OrderSummaryResponse {
    private Long id;
    private LocalDateTime createdAt;
    private OrderStatus status;
}