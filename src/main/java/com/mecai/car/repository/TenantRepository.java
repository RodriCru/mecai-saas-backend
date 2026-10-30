package com.mecai.car.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.mecai.car.models.Tenant;

/**
 * Repositorio para las busquedas de tenant y validaciones
 * TenantRepository
 */
public interface TenantRepository extends JpaRepository<Tenant, UUID>, JpaSpecificationExecutor<Tenant> {
    
    // El email es único solo entre tenants activos (índice parcial WHERE is_deleted = false)
    boolean existsByEmailAndIsDeletedFalse(String email);

    Optional <Tenant> findByEmailAndIsDeletedFalse(String email);

    boolean existsByName(String name);


}
