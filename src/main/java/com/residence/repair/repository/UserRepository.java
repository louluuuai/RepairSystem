
package com.residence.repair.repository;

import com.residence.repair.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface pour la gestion des utilisateurs.
 * Gère l'accès à la table 'users'.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Rechercher un utilisateur par son adresse e-mail.
     * Utiliser pour le processus de connexion (Auth).
     * @param email
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifier si un e-mail existe déjà dans la base de données.
     * @param email
     */
    boolean existsByEmail(String email);
}
