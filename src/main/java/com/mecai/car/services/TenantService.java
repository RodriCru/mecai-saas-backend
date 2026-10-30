package com.mecai.car.services;

import java.util.UUID;

import com.mecai.car.dto.PaginationResponseDto;
import com.mecai.car.dto.ResponseDto;
import com.mecai.car.dto.tenants.CreateTenantDto;
import com.mecai.car.dto.tenants.GetTenantDto;
import com.mecai.car.models.Tenant;

public interface TenantService {

    ResponseDto<GetTenantDto> createTenant(CreateTenantDto createTenantDto);

    ResponseDto<PaginationResponseDto<GetTenantDto>> getTenants(int page, int limit, String sort, String order, Boolean isDeleted, String search);

    ResponseDto<Void> deleteTenant(UUID id);
}
