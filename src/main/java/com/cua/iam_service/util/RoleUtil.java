package com.cua.iam_service.util;

import com.cua.iam_service.entity.Role;
import com.cua.iam_service.entity.User;
import com.cua.iam_service.exception.AppException;
import com.cua.iam_service.exception.ErrorCode;
import com.cua.iam_service.repository.RoleRepository;
import com.cua.iam_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleUtil {
    private final RoleRepository roleRepository;

    public Role getRoleOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy role với ID: " + id
                ));
    }
}
