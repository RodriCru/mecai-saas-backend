package com.mecai.car.dto.users;

import java.time.Instant;
import java.util.UUID;

import com.mecai.car.models.enums.Role;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserDto {
    
    private UUID id;

    private String fullName;

    private String email;

    private Role role;

    private Instant createdAt;

    private Instant updatedAt;

    private Boolean isDeleted;


}
