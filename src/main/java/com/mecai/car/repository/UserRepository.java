package com.mecai.car.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mecai.car.models.User;
/**
 * Repository de usuarios para validaciones y realciones con su tenant
 * UserRepository
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // El email es global entre usuarios activos (índice parcial WHERE is_deleted = false),
    // por eso estas búsquedas siempre filtran por isDeleted = false.
    boolean existsByEmailAndIsDeletedFalse(String email);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    List<User> findAllByTenantIdAndIsDeletedFalse(UUID tenantId);
}
