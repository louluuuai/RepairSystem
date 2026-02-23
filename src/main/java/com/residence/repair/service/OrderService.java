/**
 * @file OrderService.java
 * @author Xuelu AI
 */
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final RepairOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    /**
     * Obtenir l'email de l'utilisateur actuel en toute sécurité.
     */
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("Security Context Error: No authenticated principal found");
            throw new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return auth.getName();
    }

    /**
     * Création d'une commande par tenant.
     */
    @Transactional
    public void createOrder(CreateOrderRequest request) {
        String email = getCurrentUserEmail();
        log.info("User '{}' is creating a new repair order.", email);

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
        log.info("Order successfully saved for user '{}'.", email);
    }

    /**
     * Annulation une commande par le locataire avant planification.
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        String email = getCurrentUserEmail();
        log.info("User '{}' is attempting to cancel order #{}", email, orderId);

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
        log.info("Order #{} cancelled by user '{}'.", orderId, email);
    }
    /**
     * Planification par admin.
     */
    @Transactional
    public void scheduleOrder(Long orderId, OrderScheduleRequest request) {
        String email = getCurrentUserEmail();
        log.info("User '{}' is attempting to plan order #{}", email, orderId);
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        if (order.getStatus() != OrderStatus.EN_ATTENTE) {
            throw new ApiException("INVALID_TRANSITION", "Order cannot be scheduled", HttpStatus.BAD_REQUEST);
        }

        order.setScheduledAt(request.getScheduledAt());
        order.setStatus(OrderStatus.PLANIFIE);
        orderRepository.save(order);
        log.info("Order #{} planned by user '{}'.", orderId, email);
    }

    /**
     * Terminer la commande.
     */
    @Transactional
    public void completeOrder(Long orderId, UpdateOrderStatusRequest request) {

        String email = getCurrentUserEmail();
        log.info("User '{}' completes order #{}", email, orderId);
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));

        if (order.getStatus() == OrderStatus.ANNULE && request.getOrderStatus() != OrderStatus.ANNULE) {
            throw new ApiException("INVALID_ACTION", "Cannot re-activate cancelled order", HttpStatus.BAD_REQUEST);
        }

        order.setStatus(OrderStatus.TERMINE);
        orderRepository.save(order);
        log.info("Order #{} completed by user '{}'.", orderId, email);
    }

    /**
     * Obtenir la liste filtrée par rôle.
     */
    public List<OrderSummaryResponse> getAllOrders() {
        String email = getCurrentUserEmail();
        log.info("User '{}' is attempting to get all orders.", email);
        User user = userRepository.findByEmail(email).orElseThrow();

        List<RepairOrder> orders;
        if (user instanceof Admin) {
            // Admin voit tout, trié par date de création
            orders = orderRepository.findAllByOrderByCreatedAtAsc();
        } else {
            // Locataire voit ses propres demandes
            orders = orderRepository.findByTenantOrderByCreatedAtDesc((Tenant) user);
        }
        log.info("All orders found by user '{}'.", email);
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
        String email = getCurrentUserEmail();
        log.info("User '{}' is attempting to get order details #{}", email, orderId);
        User user = userRepository.findByEmail(email).orElseThrow();

        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));

        // Si c'est un locataire, il ne peut voir que son propre ordre
        if (user instanceof Tenant && !order.getTenant().getEmail().equals(email)) {
            throw new ApiException("ACCESS_DENIED", "You cannot view this order", HttpStatus.FORBIDDEN);
        }
        log.info("Order details found by user '{}'.", email);
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