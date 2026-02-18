package com.residence.repair.repository;

import com.residence.repair.domain.entity.RepairOrder;
import com.residence.repair.domain.entity.Tenant;
import com.residence.repair.domain.enums.OrderStatus;
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
    List<RepairOrder> findByTenant(Tenant tenant);

    /**
     * Trouver toutes les commandes par statut.
     * @param status
     */
    List<RepairOrder> findByStatus(OrderStatus status);

    /**
     * Trouver toutes les commandes triées par date de création.
     */
    List<RepairOrder> findAllByOrderByCreatedAtAsc();
    /**
     * Trouver toutes les commandes triés par date (Ordre décroissant).
     * @param tenant
     */
    List<RepairOrder> findByResidentOrderByCreatedAtDesc(Tenant tenant);
}
