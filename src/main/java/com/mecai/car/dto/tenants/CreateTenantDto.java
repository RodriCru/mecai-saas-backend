package com.mecai.car.dto.tenants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTenantDto {
    private String name;

    private String email;
}
