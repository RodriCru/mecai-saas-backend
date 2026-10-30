package com.mecai.car.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mecai.car.dto.PaginationResponseDto;
import com.mecai.car.dto.ResponseDto;
import com.mecai.car.dto.tenants.CreateTenantDto;
import com.mecai.car.dto.tenants.GetTenantDto;
import com.mecai.car.services.TenantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/tenants")
public class TenantController {
    private final TenantService tenantService;

    @GetMapping
    public ResponseEntity<ResponseDto<PaginationResponseDto<GetTenantDto>>> getTenants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) String search) {
        ResponseDto<PaginationResponseDto<GetTenantDto>> response =
            tenantService.getTenants(page, limit, sort, order, isDeleted, search);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    public ResponseEntity<ResponseDto<GetTenantDto>> createTenant(@RequestBody CreateTenantDto createTenantDto) {
        ResponseDto<GetTenantDto> response = tenantService.createTenant(createTenantDto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteTenant(@PathVariable UUID id) {
        ResponseDto<Void> response = tenantService.deleteTenant(id);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}
