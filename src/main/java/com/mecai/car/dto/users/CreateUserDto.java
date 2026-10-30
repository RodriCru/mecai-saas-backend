package com.mecai.car.dto.users;

import java.util.UUID;

import com.mecai.car.models.enums.Role;

import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDto {

    @NotNull
    private String fullName;

    @NotNull
    private String email;

    @NotNull
    private Role role;

    // null = usuario sin tenant (SUPERADMIN)
    private UUID tenantId;
}
