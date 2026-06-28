package com.codelogium.ticketing.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.codelogium.ticketing.common.Result;
import com.codelogium.ticketing.dto.request.UserRegisterRequestDTO;
import com.codelogium.ticketing.dto.response.UserInfoResponseDTO;
import com.codelogium.ticketing.entity.enums.UserRole;
import com.codelogium.ticketing.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@Tag(name = "User Controller", description = "用户管理")
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    // ──────────────────── 公开接口 ────────────────────

    @Operation(summary = "注册用户")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<?> registerUser(@RequestBody @Valid UserRegisterRequestDTO dto) {
        userService.createUser(dto);
        return Result.success("注册成功");
    }

    // ──────────────────── ADMIN 管理接口 ────────────────────

    @Operation(summary = "用户列表（ADMIN）")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public Result<Page<UserInfoResponseDTO>> listUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.listUsers(username, role,
                PageRequest.of(page, size)));
    }

    @Operation(summary = "查询用户详情（ADMIN）")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{userId}")
    public Result<UserInfoResponseDTO> retrieveUser(@PathVariable Long userId) {
        return Result.success(userService.retrieveUser(userId));
    }

    @Operation(summary = "修改用户角色（ADMIN）")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{userId}/role")
    public Result<?> updateUserRole(@PathVariable Long userId,
                                     @RequestParam UserRole role) {
        userService.updateUserRole(userId, role);
        return Result.success("角色修改成功");
    }

    @Operation(summary = "删除用户（ADMIN）")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{userId}")
    public Result<?> removeUser(@PathVariable Long userId) {
        userService.removeUser(userId);
        return Result.success("删除成功");
    }
}
