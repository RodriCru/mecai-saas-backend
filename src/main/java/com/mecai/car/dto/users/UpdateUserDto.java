package com.mecai.car.dto.users;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

// Sin role, tenantId ni isDeleted: esos campos nunca se editan desde aquí (evita asignación masiva)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
    private String fullName;

    private String email;

    private String password;

    // Solo se exige en /users/me cuando se cambia la contraseña propia
    private String currentPassword;
}
