package com.mecai.car.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mecai.car.dto.PaginationResponseDto;
import com.mecai.car.dto.ResponseDto;
import com.mecai.car.dto.tenants.CreateTenantDto;
import com.mecai.car.dto.tenants.GetTenantDto;
import com.mecai.car.exceptions.CreationError;
import com.mecai.car.exceptions.DuplicatedError;
import com.mecai.car.exceptions.ForbiddenError;
import com.mecai.car.exceptions.NotFoundError;
import com.mecai.car.exceptions.ServerError;
import com.mecai.car.models.Tenant;
import com.mecai.car.models.User;
import com.mecai.car.models.enums.Role;
import com.mecai.car.repository.TenantRepository;
import com.mecai.car.repository.UserRepository;
import com.mecai.car.repository.specs.TenantSpecs;
import com.mecai.car.services.TenantService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService{
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    private static final Set<String> SORT_FIELDS = Set.of("name", "email", "createdAt", "updatedAt");
    private static final int MAX_LIMIT = 100;

    @Override
    public ResponseDto<GetTenantDto> createTenant(CreateTenantDto createTenantDto){
        if(createTenantDto.getEmail()== null || createTenantDto.getName()==null){
            throw new CreationError("Email y nombre son obligatorios");
        }

        String email = createTenantDto.getEmail().trim().toLowerCase();
        if(tenantRepository.existsByEmailAndIsDeletedFalse(email)){
            throw new DuplicatedError("El email ya se encuentra asociado a un tenant");
        }

        User caller = getAuthenticatedUser();
        if(caller.getRole() != Role.SUPERADMIN){
            throw new ForbiddenError("No tienes permiso para crear tenants");
        }

        try {
            Tenant tenant = new Tenant();
            tenant.setEmail(createTenantDto.getEmail());
            tenant.setName(createTenantDto.getName());
            tenant.setCreatedAt(Instant.now());
            tenant.setUpdatedAt(Instant.now());
            tenant.setIsDeleted(false);

            Tenant created = tenantRepository.save(tenant);

            GetTenantDto result = new GetTenantDto(
                created.getId(),
                created.getName(),
                created.getEmail(),
                created.getCreatedAt(),
                created.getUpdatedAt(),
                created.getIsDeleted()
            );

            return new ResponseDto<>(HttpStatus.CREATED.value(), "Tenant Creado Exitosamente", result);
        } catch (Exception e) {
             throw new ServerError("Error interno del servidor.");
        }
    }

    @Override
    public ResponseDto<PaginationResponseDto<GetTenantDto>> getTenants(int page, int limit, String sort, String order, Boolean isDeleted, String search){
        User caller = getAuthenticatedUser();
        if(caller.getRole() != Role.SUPERADMIN){
            throw new ForbiddenError("No tienes permiso para listar tenants");
        }

        // Solo se ordena por campos permitidos, nunca por lo que mande el cliente tal cual
        String sortField = SORT_FIELDS.contains(sort) ? sort : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int safePage = Math.max(page, 1);
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(direction, sortField));

        Specification<Tenant> spec = Specification
            .where(TenantSpecs.isDeleted(isDeleted))
            .and(TenantSpecs.search(search));

        Page<Tenant> result = tenantRepository.findAll(spec, pageable);

        List<GetTenantDto> items = result.getContent().stream()
            .map(t -> new GetTenantDto(t.getId(), t.getName(), t.getEmail(), t.getCreatedAt(), t.getUpdatedAt(), t.getIsDeleted()))
            .toList();

        PaginationResponseDto<GetTenantDto> data =
            new PaginationResponseDto<>((int) result.getTotalElements(), safePage, safeLimit, items);

        return new ResponseDto<>(HttpStatus.OK.value(), "Tenants obtenidos exitosamente", data);
    }

    @Override
    public ResponseDto<Void> deleteTenant(UUID id){
        User caller = getAuthenticatedUser();
        if(caller.getRole() != Role.SUPERADMIN){
            throw new ForbiddenError("No tienes permiso para eliminar tenants");
        }

        Tenant tenat = new Tenant();

        try {
            tenat.setIsDeleted(true);
            tenat.setUpdatedAt(Instant.now());
            tenantRepository.save(tenat);

            return new ResponseDto<>(HttpStatus.OK.value(), "Tenant eliminado correctamente", null);
        } catch (Exception e) {
            throw new ServerError("Error al eliminar el tenant.");
        }
        
    }


    // Usuario autenticado cargado desde BD: si le cambian el rol o lo dan de baja, aplica de inmediato (NotFoundError -> 404)
    private User getAuthenticatedUser(){
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        return userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new NotFoundError("Usuario no encontrado"));
    }
}
