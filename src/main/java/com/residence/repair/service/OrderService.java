package com.residence.repair.service;

import com.residence.repair.domain.entity.*;
import com.residence.repair.domain.enums.OrderStatus;
import com.residence.repair.dto.request.CreateOrderRequest;
import com.residence.repair.dto.request.OrderScheduleRequest;
import com.residence.repair.dto.request.UpdateOrderStatusRequest;
import com.residence.repair.dto.response.OrderResponse;
import com.residence.repair.repository.MediaRepository;
import com.residence.repair.repository.RepairOrderRepository;
import com.residence.repair.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des commandes.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final RepairOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    /**
     * Création d'une commande par tenant.
     */
    @Transactional
    public void createOrder(CreateOrderRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tenant tenant = (Tenant) userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        RepairOrder order = new RepairOrder();
        order.setDescription(request.getDescription());
        order.setEntryAuthorized(request.getEntryAuthorized());
        order.setEntryNote(request.getEntryNote());
        order.setTenant(tenant);
        order.setStatus(OrderStatus.EN_ATTENTE);
        
        // Sauvegarde initiale pour générer l'ID
        RepairOrder savedOrder = orderRepository.save(order);

        // Liaison des médias
        if (request.getMediaList() != null && !request.getMediaList().isEmpty()) {
            request.getMediaList().forEach(media -> {
                media.setRepairOrder(savedOrder);
                mediaRepository.save(media);
            });
        }
    }

    /**
     * Annulation une commande par le locataire avant planification.
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Sécurité : Seul le propriétaire peut annuler
        if (!order.getTenant().getEmail().equals(email)) {
            throw new RuntimeException("Access denied: You are not the owner of this order");
        }

        // Vérification du statut
        if (order.getStatus() != OrderStatus.EN_ATTENTE) {
            throw new RuntimeException("Cannot cancel order: Order is already " + order.getStatus());
        }

        order.setStatus(OrderStatus.ANNULE);
        orderRepository.save(order);
    }
    /**
     * Planification par admin.
     */
    @Transactional
    public void scheduleOrder(Long orderId, OrderScheduleRequest request) {

        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.EN_ATTENTE) {
            throw new IllegalStateException("Invalid status transition");
        }

        order.setScheduledAt(request.getScheduledAt());
        order.setStatus(OrderStatus.PLANIFIE);
        orderRepository.save(order);
    }

    /**
     * Mettre à jour le statut de la commande (ex: Terminer la commande).
     */
    @Transactional
    public void updateStatus(Long orderId, UpdateOrderStatusRequest request) {

        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.ANNULE && request.getOrderStatus() != OrderStatus.ANNULE) {
            throw new RuntimeException("Cannot re-activate a cancelled order");
        }

        order.setStatus(request.getOrderStatus());
        orderRepository.save(order);
    }

    /**
     * Obtenir la liste filtrée par rôle.
     */
    public List<OrderResponse> getAllOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<RepairOrder> orders;
        if (user instanceof Admin) {
            // Admin voit tout, trié par date de création
            orders = orderRepository.findAllByOrderByCreatedAtAsc();
        } else {
            // Locataire voit ses propres demandes
            orders = orderRepository.findByResidentOrderByCreatedAtDesc((Tenant) user);
        }
        // Conversion cruciale : Entity -> DTO
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapper l'entité vers OrderResponse.
     */
    private OrderResponse mapToResponse(RepairOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .description(order.getDescription())
                .status(order.getStatus())
                .entryAuthorized(order.getEntryAuthorized())
                .entryNote(order.getEntryNote())
                .createdAt(order.getCreatedAt())
                .scheduledAt(order.getScheduledAt())
                .mediaList(order.getMediaList().stream()
                        .map(m -> OrderResponse.MediaResponse.builder()
                                .url(m.getUrl())
                                .type(m.getMediaType())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}