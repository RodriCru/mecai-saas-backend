package com.mecai.car.config;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mecai.car.models.User;
import com.mecai.car.models.enums.Role;
import com.mecai.car.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seed que crea el SUPERADMIN inicial al arrancar la app.
 * Las credenciales vienen de SEED_SUPERADMIN_EMAIL y SEED_SUPERADMIN_PASSWORD (.env);
 * si faltan, no hace nada. Es idempotente: si el email ya existe, no lo toca.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.superadmin.email:}")
    private String email;

    @Value("${seed.superadmin.password:}")
    private String password;

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }

        if (userRepository.existsByEmailAndIsDeletedFalse(email)) {
            log.info("Seed SUPERADMIN omitido: el usuario ya existe");
            return;
        }

        User user = new User();
        user.setTenant(null); // un SUPERADMIN va sin tenant
        user.setFullName("Super Admin");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.SUPERADMIN);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        log.info("Seed SUPERADMIN creado");
    }
}
