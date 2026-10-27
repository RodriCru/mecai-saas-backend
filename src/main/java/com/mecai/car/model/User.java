package com.mecai.car.model;

import java.time.Instant;
import java.util.UUID;

import com.mecai.car.model.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name="users", uniqueConstraints = @UniqueConstraint(columnNames = {"email", "tenant_id"}))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = true) 
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Tenant tenant;

    @Column(name= "full_name", nullable = false)
    private String fullName;

    @Column(name= "email", nullable = false)
    private String email;

    @Column(name= "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
}
