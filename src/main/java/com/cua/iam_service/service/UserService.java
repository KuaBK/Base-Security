package com.cua.iam_service.service;

import com.cua.iam_service.dto.response.UserResponse;
import com.cua.iam_service.entity.Role;
import com.cua.iam_service.entity.User;
import com.cua.iam_service.exception.AppException;
import com.cua.iam_service.exception.ErrorCode;
import com.cua.iam_service.repository.RoleRepository;
import com.cua.iam_service.repository.UserRepository;
import com.cua.iam_service.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserUtil userUtil;

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "không tìm thấy người dúng với email: " + email ));
        return mapToUserResponse(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    @Transactional
    public UserResponse addRoleToUser(Long userId, Long roleId) {
        User user = userUtil.getUserOrThrow(userId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy role với ID: " + roleId));

        user.getRoles().add(role);
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Transactional
    public UserResponse removeRoleFromUser(Long userId, Long roleId) {
        User user = userUtil.getUserOrThrow(userId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy role với ID: " + roleId));

        user.getRoles().remove(role);
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy user với ID: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse mapToUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roleNames)
                .build();
    }
}
