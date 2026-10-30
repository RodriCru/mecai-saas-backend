package com.mecai.car.repository.specs;

import org.springframework.data.jpa.domain.Specification;

import com.mecai.car.models.Tenant;

/**
 * Condiciones reutilizables para filtrar tenants.
 * Si el filtro viene vacío devuelven null y JPA las ignora.
 * TenantSpecs
 */
public class TenantSpecs {

    private TenantSpecs() {}

    public static Specification<Tenant> isDeleted(Boolean isDeleted) {
        return (root, query, cb) ->
            isDeleted == null ? null : cb.equal(root.get("isDeleted"), isDeleted);
    }

    public static Specification<Tenant> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String like = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("email")), like)
            );
        };
    }
}
