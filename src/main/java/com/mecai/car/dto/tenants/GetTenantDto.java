package com.mecai.car.dto.tenants;

import java.time.Instant;
import java.util.UUID;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTenantDto {
    private UUID id;

    private String name;

    private String email;

    private Instant createdAt;

    private Instant updatedAt;

    private Boolean isDeleted;

    
}
