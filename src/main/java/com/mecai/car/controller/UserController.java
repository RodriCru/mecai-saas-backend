package com.mecai.car.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mecai.car.dto.ResponseDto;
import com.mecai.car.dto.users.CreateUserDto;
import com.mecai.car.dto.users.GetUserDto;
import com.mecai.car.dto.users.UpdateUserDto;
import com.mecai.car.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ResponseDto<GetUserDto>> createUser(@RequestBody CreateUserDto createUserDto) {
        ResponseDto<GetUserDto> response = userService.createUser(createUserDto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<GetUserDto>> meProfile() {
        ResponseDto<GetUserDto> response = userService.meProfile();
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<ResponseDto<GetUserDto>> updateMe(@RequestBody UpdateUserDto updateUserDto) {
        ResponseDto<GetUserDto> response = userService.updateMe(updateUserDto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDto<GetUserDto>> updateUser(@PathVariable UUID id,
            @RequestBody UpdateUserDto updateUserDto) {
        ResponseDto<GetUserDto> response = userService.updateUser(id, updateUserDto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteUser(@PathVariable UUID id) {
        ResponseDto<Void> response = userService.deleteUser(id);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}
