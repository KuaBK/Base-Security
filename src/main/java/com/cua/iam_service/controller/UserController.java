package com.cua.iam_service.controller;

import com.cua.iam_service.dto.response.UserResponse;
import com.cua.iam_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API quản lý người dùng")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Lấy danh sách người dùng")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin người dùng theo ID")
    @PreAuthorize("hasRole('ADMIN') or principal.username == @userService.getUserById(#id).email")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "Thêm role cho người dùng")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> addRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        UserResponse user = userService.addRoleToUser(userId, roleId);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "Xóa role khỏi người dùng")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        UserResponse user = userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}