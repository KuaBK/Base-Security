package com.cua.iam_service.controller;

import com.cua.iam_service.dto.BaseResponse;
import com.cua.iam_service.dto.response.UserResponse;
import com.cua.iam_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<BaseResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Lấy danh sách người dùng thành công", users));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<UserResponse>> getUserByEmail(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Lấy thông tin người dùng thành công", user));
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "Thêm role cho người dùng")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserResponse>> addRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        UserResponse user = userService.addRoleToUser(userId, roleId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Thêm role cho người dùng thành công", user));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "Xóa role khỏi người dùng")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserResponse>> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        UserResponse user = userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok("Xóa role cho người dùng thành công", user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.noContent("Xóa người dùng thành công"));
    }
}