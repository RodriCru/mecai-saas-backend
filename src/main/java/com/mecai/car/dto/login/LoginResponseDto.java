package com.mecai.car.dto.login;

import com.mecai.car.models.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ayuda a la respuesta cuando se hace un login exitoso.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String name;
    private Role role;
    private String accessToken;
    private String refreshToken;
}
