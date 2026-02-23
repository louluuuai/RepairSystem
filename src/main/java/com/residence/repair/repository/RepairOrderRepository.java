package com.residence.repair.repository;

import com.residence.repair.domain.entity.RepairOrder;
import com.residence.repair.domain.entity.Tenant;
import com.residence.repair.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * Interface pour la gestion des commandes de réparation.
 * Gère l'accès à la table 'repair_orders'.
 */
@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {
    /**
     * Trouver toutes les commandes d'un locataire spécifique.
     */
    Page<RepairOrder> findByTenant(Tenant tenant, Pageable pageable);

    /**
     * Trouver toutes les commandes par statut.
     */
    Page<RepairOrder> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Trouver toutes les commandes.
     */
    Page<RepairOrder> findAll(Pageable pageable);
}
