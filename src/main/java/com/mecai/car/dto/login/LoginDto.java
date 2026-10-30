package com.mecai.car.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que se encarga del login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    private String email;
    private String contrasena;
}
