package com.cua.iam_service.service;

import com.cua.iam_service.dto.BaseResponse;
import com.cua.iam_service.entity.OtpToken;
import com.cua.iam_service.entity.User;
import com.cua.iam_service.exception.AppException;
import com.cua.iam_service.exception.ErrorCode;
import com.cua.iam_service.repository.OtpTokenRepository;
import com.cua.iam_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.cooldown-minutes}")
    private int cooldownMinutes;

    private final SecureRandom random = new SecureRandom();

    private String createOtp(String email, OtpToken.OtpType otpType) {
        if (!userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Email không tồn tại trong hệ thống");
        }

        Optional<OtpToken> existing = otpTokenRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
                        email, otpType);

        if (existing.isPresent() && existing.get().getCreatedAt()
                .isAfter(LocalDateTime.now().minusMinutes(cooldownMinutes))) {
            throw new AppException(ErrorCode.WAITING_FOR_OTP);
        }

        // Vô hiệu hóa tất cả OTP cũ của email này
        otpTokenRepository.markAllAsUsedByEmailAndType(email, otpType);

        // Tạo mã OTP mới
        String otpCode = generateRanDomOtpCode();

        // Lưu OTP vào database
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email);
        otpToken.setOtpCode(otpCode);
        otpToken.setType(otpType);
        otpToken.setExpiryTime(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        otpTokenRepository.save(otpToken);

        return otpCode;
    }

    // ====================== QUÊN MẬT KHẨU ======================

    @Transactional
    public BaseResponse<Void> sendForgotPasswordOtp(String email) {
        String otpCode = createOtp(email, OtpToken.OtpType.FORGOT_PASSWORD);
        emailService.sendForgotPasswordOtp(email, otpCode);

        return BaseResponse.ok("Mã OTP đã được gửi đến email của bạn", null);
    }

    public BaseResponse<Void> verifyForgotPasswordOtp(String email, String otpCode) {
        Optional<OtpToken> optionalToken = otpTokenRepository
                .findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(email, otpCode, LocalDateTime.now());

        if (optionalToken.isPresent()) {
            OtpToken token = optionalToken.get();
            if (token.getType() == OtpToken.OtpType.FORGOT_PASSWORD) {
                token.setUsed(true);
                otpTokenRepository.save(token);
                return BaseResponse.ok("Mã OTP hợp lệ. Bạn có thể đặt lại mật khẩu", null);
            }
        }
        return BaseResponse.badRequest("Mã OTP không hợp lệ hoặc đã hết hạn");
    }

    @Transactional
    public BaseResponse<Void> resetPassword(String email, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            return BaseResponse.badRequest("Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thầy người dùng với email" + email));
        user.setPassword(passwordEncoder.encode(newPassword));

        return BaseResponse.ok("Đặt lại mật khẩu thành công", null);

    }

    // ====================== XÁC THỰC EMAIL ======================

    @Transactional
    public BaseResponse<Void> sendEmailVerificationOtp(String email) {
        String otpCode = createOtp(email, OtpToken.OtpType.EMAIL_VERIFICATION);
        emailService.sendEmailVerificationOtp(email, otpCode);
        return BaseResponse.ok("Mã OTP xác thực đã được gửi đến email của bạn", null);
    }

    @Transactional
    public BaseResponse<Void> verifyEmail(String email, String otpCode) {
        Optional<OtpToken> optionalToken = otpTokenRepository
                .findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(email, otpCode, LocalDateTime.now());

        if (optionalToken.isPresent()) {
            OtpToken token = optionalToken.get();
            if (token.getType() == OtpToken.OtpType.EMAIL_VERIFICATION) {
                token.setUsed(true);
                otpTokenRepository.save(token);

                return BaseResponse.ok("Xác thực email thành công", null);
            }
        }
        return BaseResponse.badRequest("Mã OTP không hợp lệ hoặc đã hết hạn");
    }

    // ====================== ĐỔI MẬT KHẨU ======================

    @Transactional
    public BaseResponse<Void> requestChangePassword(String email) {
        String otpCode = createOtp(email, OtpToken.OtpType.PASSWORD_RESET);
        emailService.sendChangePasswordOtp(email, otpCode);
        return BaseResponse.ok("Mã OTP đã được gửi đến email của bạn để xác nhận đổi mật khẩu", null);
    }

    public BaseResponse<Void> verifyPasswordResetOtp(String email, String otpCode) {
        Optional<OtpToken> optionalToken = otpTokenRepository
                .findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(email, otpCode, LocalDateTime.now());

        if (optionalToken.isPresent()) {
            OtpToken token = optionalToken.get();
            if (token.getType() == OtpToken.OtpType.PASSWORD_RESET) {
                token.setUsed(true);
                otpTokenRepository.save(token);
                return BaseResponse.ok("Mã OTP hợp lệ. Bạn có thể đặt lại mật khẩu", null);
            }
        }
        return BaseResponse.badRequest("Mã OTP không hợp lệ hoặc đã hết hạn");
    }

    @Transactional
    public BaseResponse<Void> confirmChangePassword(String email, String newPassword, String confirmPassword) {
        // Kiểm tra mật khẩu xác nhận
        if (!newPassword.equals(confirmPassword)) {
            return BaseResponse.badRequest("Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thầy người dùng với email" + email));
        user.setPassword(passwordEncoder.encode(newPassword));

        return BaseResponse.ok("Đổi mật khẩu thành công", null);
    }

    private String generateRanDomOtpCode() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        otpTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
