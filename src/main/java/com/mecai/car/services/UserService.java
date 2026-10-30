package com.mecai.car.services;

import java.util.UUID;

import com.mecai.car.dto.ResponseDto;
import com.mecai.car.dto.login.LoginDto;
import com.mecai.car.dto.login.LoginResponseDto;
import com.mecai.car.dto.login.RefreshTokenDto;
import com.mecai.car.dto.login.RefreshTokenResponse;
import com.mecai.car.dto.users.CreateUserDto;
import com.mecai.car.dto.users.GetUserDto;
import com.mecai.car.dto.users.UpdateUserDto;


public interface UserService {
    ResponseDto<GetUserDto> createUser(CreateUserDto createUserDto);

    ResponseDto<GetUserDto> meProfile();

    // PATCH /users/me: el id siempre sale del token
    ResponseDto<GetUserDto> updateMe(UpdateUserDto updateUserDto);

    // PATCH /users/{id}: solo SUPERADMIN, u OWNER sobre personal de su tenant
    ResponseDto<GetUserDto> updateUser(UUID id, UpdateUserDto updateUserDto);

    ResponseDto<Void> deleteUser(UUID id);

    LoginResponseDto login(LoginDto request);

    RefreshTokenResponse refreshToken(RefreshTokenDto request);

    void logout(String token);
}
