package com.mecai.car.models;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Tabla que modela los tenants, solo es para registro e ir filtrando.
 * 
 * Tenant
 */
@Data
@Entity
// El email es único entre tenants activos: lo garantiza el índice parcial
// tenants_email_active_uk (WHERE is_deleted = false), que JPA no puede declarar. Ver db/users_email_unique.sql
@Table(name="tenants")
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="email", nullable = false)
    private String email;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now(); // Esto ya es UTC
    }


    @Column(name="updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    
}
