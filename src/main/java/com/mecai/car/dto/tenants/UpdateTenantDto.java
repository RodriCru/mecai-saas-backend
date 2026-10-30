package com.mecai.car.dto.tenants;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTenantDto {
    
    private String name;

    private String email;
}
