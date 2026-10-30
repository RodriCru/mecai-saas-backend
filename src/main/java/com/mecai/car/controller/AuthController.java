package com.mecai.car.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mecai.car.dto.login.LoginDto;
import com.mecai.car.dto.login.LoginResponseDto;
import com.mecai.car.dto.login.RefreshTokenDto;
import com.mecai.car.dto.login.RefreshTokenResponse;
import com.mecai.car.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(userService.login(loginDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return ResponseEntity.ok(userService.refreshToken(refreshTokenDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader.startsWith(BEARER_PREFIX)) {
            userService.logout(authHeader.substring(BEARER_PREFIX.length()));
        }
        return ResponseEntity.noContent().build();
    }
}
