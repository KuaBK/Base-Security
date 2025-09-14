package com.cua.iam_service.util;

import com.cua.iam_service.entity.User;
import com.cua.iam_service.exception.AppException;
import com.cua.iam_service.exception.ErrorCode;
import com.cua.iam_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtil {
    private final UserRepository userRepository;

    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy user với ID: " + id
                ));
    }
}
