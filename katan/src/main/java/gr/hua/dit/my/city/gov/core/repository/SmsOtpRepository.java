package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.SmsOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for {@link SmsOtp} entity.
 */
@Repository
public interface SmsOtpRepository extends JpaRepository<SmsOtp, String> {

    /**
     * Find an SMS OTP by phone number.
     *
     * @param phoneNumber the phone number
     * @return Optional containing the SmsOtp if found
     */
    Optional<SmsOtp> findByPhoneNumber(final String phoneNumber);

    /**
     * Delete all expired OTPs based on the given expiration time.
     *
     * @param expirationTime the expiration time threshold
     */
    void deleteByExpiresAtBefore(final LocalDateTime expirationTime);

    /**
     * Check if an OTP exists for the given phone number.
     *
     * @param phoneNumber the phone number
     * @return true if OTP exists, false otherwise
     */
    boolean existsByPhoneNumber(final String phoneNumber);
}
