package com.mecai.car.dto.login;

import lombok.Builder;
import lombok.Data;

/**
 * DTO que se encarga cuando se quiere gtenerar un uevo access token.
 */
@Data
@Builder
public class RefreshTokenDto {
    private String refreshToken;
}
