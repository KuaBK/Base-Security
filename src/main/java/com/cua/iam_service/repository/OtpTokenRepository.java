package com.cua.iam_service.repository;

import com.cua.iam_service.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(
            String email, String otpCode, LocalDateTime currentTime);

    Optional<OtpToken> findByEmailAndTypeAndUsedFalseAndExpiryTimeAfter(
            String email, OtpToken.OtpType type, LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiryTime < :currentTime")
    void deleteExpiredTokens(LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE OtpToken o SET o.used = true WHERE o.email = :email AND o.type = :type AND o.used = false")
    void markAllAsUsedByEmailAndType(String email, OtpToken.OtpType type);

    Optional<OtpToken> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
            String email, OtpToken.OtpType type);
}