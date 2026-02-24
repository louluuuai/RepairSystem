package com.residence.repair.service;

import com.residence.repair.domain.entity.Admin;
import com.residence.repair.domain.entity.Media;
import com.residence.repair.domain.entity.RepairOrder;
import com.residence.repair.domain.entity.Tenant;
import com.residence.repair.domain.enums.MediaType;
import com.residence.repair.domain.enums.OrderStatus;
import com.residence.repair.dto.request.CreateOrderRequest;
import com.residence.repair.dto.request.OrderScheduleRequest;
import com.residence.repair.dto.request.UpdateOrderStatusRequest;
import com.residence.repair.dto.response.PageResponse;
import com.residence.repair.exception.ApiException;
import com.residence.repair.repository.MediaRepository;
import com.residence.repair.repository.RepairOrderRepository;
import com.residence.repair.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private RepairOrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private OrderService orderService;

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrder_shouldSetEnAttenteAndFranceCreatedAtAndSaveMedia() {
        setAuthenticatedUser("tenant@test.com");
        Tenant tenant = new Tenant();
        tenant.setEmail("tenant@test.com");

        when(userRepository.findByEmail("tenant@test.com")).thenReturn(Optional.of(tenant));
        when(orderRepository.save(any(RepairOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setDescription("Machine leak");
        request.setEntryAuthorized(true);
        request.setEntryNote("Key in lockbox");

        CreateOrderRequest.MediaRequest mediaRequest = new CreateOrderRequest.MediaRequest();
        mediaRequest.setUrl("/media/file.jpg");
        mediaRequest.setType(MediaType.IMAGE);
        request.setMediaList(List.of(mediaRequest));

        LocalDateTime before = LocalDateTime.now(ZoneId.of("Europe/Paris")).minusSeconds(2);
        orderService.createOrder(request);
        LocalDateTime after = LocalDateTime.now(ZoneId.of("Europe/Paris")).plusSeconds(2);

        ArgumentCaptor<RepairOrder> orderCaptor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(orderRepository).save(orderCaptor.capture());
        RepairOrder saved = orderCaptor.getValue();

        assertEquals(OrderStatus.EN_ATTENTE, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
        assertTrue(!saved.getCreatedAt().isBefore(before) && !saved.getCreatedAt().isAfter(after));

        ArgumentCaptor<Media> mediaCaptor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(mediaCaptor.capture());
        assertEquals("/media/file.jpg", mediaCaptor.getValue().getUrl());
        assertEquals(MediaType.IMAGE, mediaCaptor.getValue().getMediaType());
    }

    @Test
    void cancelOrder_shouldSetAnnuleWhenOwnerAndEnAttente() {
        setAuthenticatedUser("tenant@test.com");
        Tenant tenant = new Tenant();
        tenant.setEmail("tenant@test.com");

        RepairOrder order = new RepairOrder();
        order.setTenant(tenant);
        order.setStatus(OrderStatus.EN_ATTENTE);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertEquals(OrderStatus.ANNULE, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void scheduleOrder_shouldSetPlanifieAndScheduledAt() {
        setAuthenticatedUser("admin@test.com");

        RepairOrder order = new RepairOrder();
        order.setStatus(OrderStatus.EN_ATTENTE);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        OrderScheduleRequest request = new OrderScheduleRequest();
        request.setScheduledAt(LocalDateTime.now().plusDays(1));

        orderService.scheduleOrder(2L, request);

        assertEquals(OrderStatus.PLANIFIE, order.getStatus());
        assertEquals(request.getScheduledAt(), order.getScheduledAt());
        verify(orderRepository).save(order);
    }

    @Test
    void completeOrder_shouldThrowWhenOrderCancelledAndRequestNotAnnule() {
        setAuthenticatedUser("admin@test.com");

        RepairOrder order = new RepairOrder();
        order.setStatus(OrderStatus.ANNULE);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setOrderStatus(OrderStatus.TERMINE);

        ApiException ex = assertThrows(ApiException.class, () -> orderService.completeOrder(3L, request));
        assertEquals("INVALID_ACTION", ex.getCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getAllOrders_shouldUseStatusFilterForAdmin() {
        setAuthenticatedUser("admin@test.com");
        Admin admin = new Admin();
        admin.setEmail("admin@test.com");

        RepairOrder order = new RepairOrder();
        order.setId(10L);
        order.setStatus(OrderStatus.EN_ATTENTE);
        order.setCreatedAt(LocalDateTime.now());

        Page<RepairOrder> page = new PageImpl<>(
                List.of(order),
                PageRequest.of(0, 10),
                1
        );

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(orderRepository.findByStatus(eq(OrderStatus.EN_ATTENTE), any())).thenReturn(page);

        PageResponse<?> response = orderService.getAllOrders(0, 10, "createdAt", org.springframework.data.domain.Sort.Direction.ASC, OrderStatus.EN_ATTENTE);

        assertEquals(1, response.getTotalElements());
        verify(orderRepository).findByStatus(eq(OrderStatus.EN_ATTENTE), any());
        verify(orderRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getOrderDetails_shouldDenyTenantReadingOthersOrder() {
        setAuthenticatedUser("tenant1@test.com");
        Tenant currentUser = new Tenant();
        currentUser.setEmail("tenant1@test.com");

        Tenant owner = new Tenant();
        owner.setEmail("tenant2@test.com");
        RepairOrder order = new RepairOrder();
        order.setTenant(owner);

        when(userRepository.findByEmail("tenant1@test.com")).thenReturn(Optional.of(currentUser));
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        ApiException ex = assertThrows(ApiException.class, () -> orderService.getOrderDetails(5L));
        assertEquals("ACCESS_DENIED", ex.getCode());
    }

    private void setAuthenticatedUser(String email) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
