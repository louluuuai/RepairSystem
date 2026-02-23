package com.residence.repair.service;

import com.residence.repair.domain.entity.*;
import com.residence.repair.domain.enums.OrderStatus;
import com.residence.repair.dto.request.CreateOrderRequest;
import com.residence.repair.dto.request.OrderScheduleRequest;
import com.residence.repair.dto.request.UpdateOrderStatusRequest;
import com.residence.repair.dto.response.OrderResponse;
import com.residence.repair.dto.response.OrderSummaryResponse;
import com.residence.repair.exception.ApiException;
import com.residence.repair.repository.MediaRepository;
import com.residence.repair.repository.RepairOrderRepository;
import com.residence.repair.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException("UNAUTHORIZED", "User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        // Vérification du rôle pour éviter ClassCastException
        if (!(user instanceof Tenant)) {
            throw new ApiException("ROLE_FORBIDDEN", "Only tenants can create orders", HttpStatus.FORBIDDEN);
        }

        RepairOrder order = new RepairOrder();
        order.setDescription(request.getDescription());
        order.setEntryAuthorized(request.getEntryAuthorized());
        order.setEntryNote(request.getEntryNote());
        order.setTenant((Tenant) user);
        order.setStatus(OrderStatus.EN_ATTENTE);

        // Sauvegarde initiale pour générer l'ID
        RepairOrder savedOrder = orderRepository.save(order);

        // Liaison des médias
        if (request.getMediaList() != null) {
            for (CreateOrderRequest.MediaRequest m : request.getMediaList()) {
                Media media = new Media();
                media.setUrl(m.getUrl());
                media.setMediaType(m.getType());
                media.setRepairOrder(savedOrder);
                mediaRepository.save(media);
            }
        }
    }

    /**
     * Annulation une commande par le locataire avant planification.
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException("UNAUTHORIZED", "User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        // Sécurité : Seul le propriétaire peut annuler
        if (!order.getTenant().getEmail().equals(email)) {
            throw new ApiException("ACCESS_DENIED", "Not the owner of this order", HttpStatus.FORBIDDEN);
        }

        // Vérification du statut
        if (order.getStatus() != OrderStatus.EN_ATTENTE) {
            throw new ApiException("INVALID_STATUS", "Order is already " + order.getStatus(), HttpStatus.BAD_REQUEST);
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
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        if (order.getStatus() != OrderStatus.EN_ATTENTE) {
            throw new ApiException("INVALID_TRANSITION", "Order cannot be scheduled", HttpStatus.BAD_REQUEST);
        }

        order.setScheduledAt(request.getScheduledAt());
        order.setStatus(OrderStatus.PLANIFIE);
        orderRepository.save(order);
    }

    /**
     * Terminer la commande.
     */
    @Transactional
    public void completeOrder(Long orderId, UpdateOrderStatusRequest request) {

        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));

        if (order.getStatus() == OrderStatus.ANNULE && request.getOrderStatus() != OrderStatus.ANNULE) {
            throw new ApiException("INVALID_ACTION", "Cannot re-activate cancelled order", HttpStatus.BAD_REQUEST);
        }

        order.setStatus(OrderStatus.TERMINE);
        orderRepository.save(order);
    }

    /**
     * Obtenir la liste filtrée par rôle.
     */
    public List<OrderSummaryResponse> getAllOrders() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException("UNAUTHORIZED", "User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
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
        return orders.stream().map(o -> OrderSummaryResponse.builder()
                .id(o.getId())
                .createdAt(o.getCreatedAt())
                .status(o.getStatus())
                .build()).collect(Collectors.toList());
    }
    /**
     * Récupérer les détails d'un ordre spécifique avec vérification de propriété.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException("UNAUTHORIZED", "User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));

        // Si c'est un locataire, il ne peut voir que son propre ordre
        if (user instanceof Tenant && !order.getTenant().getEmail().equals(email)) {
            throw new ApiException("ACCESS_DENIED", "You cannot view this order", HttpStatus.FORBIDDEN);
        }

        return mapToResponse(order);
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