package com.residence.repair.repository;

import com.residence.repair.domain.entity.Media;
import com.residence.repair.domain.entity.RepairOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface pour la gestion des médias liés aux commandes.
 * Gère l'accès à la table 'media'.
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    /**
     * Trouver tous les médias associés à une commande spécifique.
     * @param repairOrder
     */
    List<Media> findByRepairOrder(RepairOrder repairOrder);
}


