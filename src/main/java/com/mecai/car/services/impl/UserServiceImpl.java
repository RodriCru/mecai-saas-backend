package com.mecai.car.services.impl;

import java.time.Instant;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mecai.car.dto.ResponseDto;
import com.mecai.car.dto.login.LoginDto;
import com.mecai.car.dto.login.LoginResponseDto;
import com.mecai.car.dto.login.RefreshTokenDto;
import com.mecai.car.dto.login.RefreshTokenResponse;
import com.mecai.car.dto.users.CreateUserDto;
import com.mecai.car.dto.users.GetUserDto;
import com.mecai.car.dto.users.UpdateUserDto;
import com.mecai.car.exceptions.CreationError;
import com.mecai.car.exceptions.DuplicatedError;
import com.mecai.car.exceptions.ForbiddenError;
import com.mecai.car.exceptions.LoginError;
import com.mecai.car.exceptions.NotFoundError;
import com.mecai.car.exceptions.ServerError;
import com.mecai.car.jwt.JwtService;
import com.mecai.car.models.Tenant;
import com.mecai.car.models.User;
import com.mecai.car.models.enums.Role;
import com.mecai.car.repository.TenantRepository;
import com.mecai.car.repository.UserRepository;
import com.mecai.car.services.UserService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${resend.key}")
    private String key;

    @Value("${resend.email}")
    private String resendEmail;

    @Override
    public ResponseDto<GetUserDto> createUser(CreateUserDto createUserDto){
        
        if(createUserDto.getFullName() == null || createUserDto.getEmail() == null || createUserDto.getRole() == null){
            throw new CreationError("Algunos campos obligatorios estan nulos");
        }

        String email = createUserDto.getEmail().trim().toLowerCase();
        if(userRepository.existsByEmailAndIsDeletedFalse(email)){
            throw new DuplicatedError("El email ya se encuentra asociado a una cuenta");
        }

        // 3. Resolver el tenant según quién crea
        // - SUPERADMIN: un SUPERADMIN va sin tenant; cualquier otro rol lo exige, si no queda volando
        // - OWNER: solo crea personal de su propio tenant (incluidos otros OWNER); se ignora el tenantId del body
        // - Resto de roles: no crean usuarios
        User caller = getAuthenticatedUser();
        Role role = createUserDto.getRole();
        UUID tenantId = createUserDto.getTenantId();
        Tenant tenant;

        if(caller.getRole() == Role.SUPERADMIN){
            if(role == Role.SUPERADMIN){
                if(tenantId != null){
                    throw new CreationError("Un SUPERADMIN no puede tener tenant");
                }
                tenant = null;
            }else{
                if(tenantId == null){
                    throw new CreationError("El tenant es obligatorio para el rol " + role);
                }
                tenant = tenantRepository.findById(tenantId)
                    .filter(t -> !t.getIsDeleted())
                    .orElseThrow(() -> new CreationError("El tenant no existe"));
            }
        }else if(caller.getRole() == Role.OWNER){
            if(role == Role.SUPERADMIN){
                throw new ForbiddenError("No puedes crear usuarios con el rol " + role);
            }
            tenant = caller.getTenant();
        }else{
            throw new ForbiddenError("No tienes permiso para crear usuarios");
        }

        try{

            String tempPassword = RandomStringUtils.randomAlphanumeric(10);

            User user = new User();
            user.setFullName(createUserDto.getFullName().trim());
            user.setEmail(email);
            user.setRole(createUserDto.getRole());
            user.setTenant(tenant);
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setUpdatedAt(Instant.now());
            user.setCreatedAt(Instant.now());

            User saved = userRepository.save(user);

            Resend resend = new Resend(key);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Rodantex <" + resendEmail + ">")
                    .to(saved.getEmail())
                    .subject("Registro a Rodantex")
                    .html("""
                            <div>
                                <strong>¡Bienvenido a Rodantex!</strong>
                                <p>Has quedado registrado exitosamente.</p>
                                <p>Accede a la plataforma con tu correo y contraseña proporcionados a continuación:</p>
                                <p><b>Correo:</b> %s</p>
                                <p><b>Contraseña:</b> %s</p>
                            </div>
                            """.formatted(saved.getEmail(), tempPassword))
                    .build();
            try {
                CreateEmailResponse data = resend.emails().send(params);
                System.out.println(data.getId());
            } catch (ResendException e) {
                e.printStackTrace();
            }

            GetUserDto result = new GetUserDto(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getRole(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.getIsDeleted()
            );

            return new ResponseDto<>(HttpStatus.CREATED.value(), "Usuario creado correctamente", result);
        }catch(Exception e){
            throw new ServerError("Error interno del servidor.");
        }
    }

    @Override
    public ResponseDto<GetUserDto> meProfile(){
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new NotFoundError("Usuario no encontrado"));

        try {
            GetUserDto profile = new GetUserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getIsDeleted()
            );

            return new ResponseDto<GetUserDto>(HttpStatus.OK.value(), "Perfil obtenido exitosamente", profile);
        } catch (Exception e) {
            throw new ServerError("Error al obtener el perfil del usuario.");
        }
    }

    @Override
    public ResponseDto<GetUserDto> updateMe(UpdateUserDto updateUserDto){
        User user = getAuthenticatedUser();

        if(StringUtils.hasText(updateUserDto.getPassword())){
            if(!StringUtils.hasText(updateUserDto.getCurrentPassword())
                || !passwordEncoder.matches(updateUserDto.getCurrentPassword(), user.getPassword())){
                throw new ForbiddenError("La contraseña actual es incorrecta");
            }
        }

        return applyUpdate(user, updateUserDto);
    }

    @Override
    public ResponseDto<GetUserDto> updateUser(UUID id, UpdateUserDto updateUserDto){

        User caller = getAuthenticatedUser();

        if(id.equals(caller.getId())){
            throw new ForbiddenError("Para editar tu propio usuario usa /users/me");
        }

        User target = findManageableUser(caller, id);

        return applyUpdate(target, updateUserDto);
    }

    @Override
    public ResponseDto<Void> deleteUser(UUID id){

        User caller = getAuthenticatedUser();

        if(id.equals(caller.getId())){
            throw new ForbiddenError("No puedes eliminar tu propio usuario");
        }

        User target = findManageableUser(caller, id);

        try {
            target.setIsDeleted(true);
            target.setUpdatedAt(Instant.now());
            userRepository.save(target);

            return new ResponseDto<>(HttpStatus.OK.value(), "Usuario eliminado correctamente", null);
        } catch (Exception e) {
            throw new ServerError("Error al eliminar el usuario.");
        }
    }

    private User getAuthenticatedUser(){
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        return userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new NotFoundError("Usuario no encontrado"));
    }

    // Reglas de quién puede administrar a quién:
    // - SUPERADMIN: a cualquiera
    // - OWNER: solo personal de su tenant, nunca a otro OWNER ni a un SUPERADMIN
    // - Resto de roles: a nadie
    private User findManageableUser(User caller, UUID targetId){
        // 1. Primero el rol, antes de tocar la BD: así un DRIVER no puede sondear qué ids existen (ForbiddenError -> 403)
        if(caller.getRole() != Role.SUPERADMIN && caller.getRole() != Role.OWNER){
            throw new ForbiddenError("No tienes permiso para administrar usuarios");
        }

        User target = userRepository.findByIdAndIsDeletedFalse(targetId)
            .orElseThrow(() -> new NotFoundError("Usuario no encontrado"));

        if(caller.getRole() == Role.SUPERADMIN){
            return target;
        }

        // 2. OWNER fuera de su tenant -> 404, no 403: no se confirma que el id exista en otro tenant
        boolean sameTenant = caller.getTenant() != null && target.getTenant() != null
            && caller.getTenant().getId().equals(target.getTenant().getId());
        if(!sameTenant){
            throw new NotFoundError("Usuario no encontrado");
        }

        // 3. Dentro de su tenant, un OWNER no administra a otro OWNER
        if(target.getRole() == Role.OWNER || target.getRole() == Role.SUPERADMIN){
            throw new ForbiddenError("No tienes permiso para administrar este usuario");
        }

        return target;
    }

    // Lógica común de actualización: solo nombre, email y contraseña; nunca role, tenant ni isDeleted
    private ResponseDto<GetUserDto> applyUpdate(User user, UpdateUserDto updateUserDto){
        // 1. Validaciones antes del try: los errores de negocio no se enmascaran como 500 (DuplicatedError -> 409)
        String email = null;
        if(StringUtils.hasText(updateUserDto.getEmail())){
            email = updateUserDto.getEmail().trim().toLowerCase();
            if(!email.equals(user.getEmail()) && userRepository.existsByEmailAndIsDeletedFalse(email)){
                throw new DuplicatedError("El correo al que se desea actualizar ya existe, intente con otro");
            }
        }

        try {
            // 2. Aplicar solo los campos que vienen
            if(StringUtils.hasText(updateUserDto.getFullName())){
                user.setFullName(updateUserDto.getFullName().trim());
            }
            if(email != null){
                user.setEmail(email);
            }
            if(StringUtils.hasText(updateUserDto.getPassword())){
                user.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
            }
            user.setUpdatedAt(Instant.now());

            // 3. Guardar
            User saved = userRepository.save(user);

            // 4. Mapear a DTO de respuesta (sin password)
            GetUserDto result = new GetUserDto(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getRole(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.getIsDeleted()
            );

            return new ResponseDto<>(HttpStatus.OK.value(), "Usuario actualizado correctamente", result);
        } catch (Exception e) {
            throw new ServerError("Error al actualizar el usuario.");
        }
    }

    @Override
    public LoginResponseDto login(LoginDto request){
        if (!StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getContrasena())) {
            throw new LoginError("Credenciales invalidas");
        }

        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .filter(u -> passwordEncoder.matches(request.getContrasena(), u.getPassword()))
                .orElseThrow(() -> new LoginError("Credenciales invalidas"));

        // Si el tenant está dado de baja, su personal tampoco entra
        if (user.getTenant() != null && user.getTenant().getIsDeleted()) {
            throw new LoginError("Credenciales invalidas");
        }

        return new LoginResponseDto(
                user.getFullName(),
                user.getRole(),
                jwtService.generateAccessToken(user.getId()),
                jwtService.generateRefreshToken(user.getId()));
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenDto request){
        String refreshToken = request.getRefreshToken();
        if (!StringUtils.hasText(refreshToken)) {
            throw new LoginError("Refresh token inválido o expirado");
        }

        UUID userId;
        try {
            String id = jwtService.extractUserId(refreshToken);
            if (!jwtService.isTokenValid(refreshToken, id)) {
                throw new LoginError("Refresh token inválido o expirado");
            }
            userId = UUID.fromString(id);
        } catch (JwtException | IllegalArgumentException e) {
            throw new LoginError("Refresh token inválido o expirado");
        }

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new LoginError("Refresh token inválido o expirado"));

        // Igual que en login: si el tenant está dado de baja, su personal tampoco renueva sesión
        if (user.getTenant() != null && user.getTenant().getIsDeleted()) {
            throw new LoginError("Refresh token inválido o expirado");
        }

        // Rotación: el refresh token usado ya no vuelve a servir
        jwtService.revokeToken(refreshToken);

        return new RefreshTokenResponse(
                jwtService.generateAccessToken(user.getId()),
                jwtService.generateRefreshToken(user.getId()));
    }


    @Override
    public void logout(String token){
        try{
            jwtService.revokeToken(token);
        }catch(JwtException | IllegalArgumentException e){
            // Token inválido o ya expirado: ya no sirve, no hay nada que revocar
        }
    }
}
