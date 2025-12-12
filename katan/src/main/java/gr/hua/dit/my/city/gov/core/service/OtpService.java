package gr.hua.dit.my.city.gov.core.service;

import gr.hua.dit.my.city.gov.core.model.SmsOtp;
import gr.hua.dit.my.city.gov.core.repository.SmsOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final SmsOtpRepository repo;
    private final SmsSender smsSender;

    public OtpService(SmsOtpRepository repo, SmsSender smsSender) {
        this.repo = repo;
        this.smsSender = smsSender;
    }

    public void generateOtp(String phoneNumber) {
        logger.debug("generateOtp called with phone: '{}'", phoneNumber);
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        SmsOtp smsOtp = new SmsOtp();
        smsOtp.setPhoneNumber(phoneNumber);
        smsOtp.setOtp(otp);
        smsOtp.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        repo.save(smsOtp);
        logger.debug("OTP saved to database: phone='{}', otp='{}'", phoneNumber, otp);

        smsSender.sendSms(phoneNumber, "Your login code is: " + otp);
    }

    /**
     * Validates the OTP for the given phone number.
     *
     * @param phoneNumber the phone number
     * @param otp the OTP to validate
     * @return true if the OTP is valid and not expired, false otherwise
     */
    public boolean validateOtp(String phoneNumber, String otp) {
        logger.debug("validateOtp called with phone: '{}', otp: '{}'", phoneNumber, otp);
        var record = repo.findByPhoneNumber(phoneNumber);

        if (record.isEmpty()) {
            logger.debug("No OTP record found for phone: '{}'", phoneNumber);
            return false;
        }

        SmsOtp smsOtp = record.get();
        logger.debug("Found OTP record. Stored OTP: '{}', Provided OTP: '{}', Expires at: {}", 
                    smsOtp.getOtp(), otp, smsOtp.getExpiresAt());

        // Check if OTP has expired
        if (smsOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.debug("OTP expired for phone: '{}'", phoneNumber);
            repo.deleteById(phoneNumber);
            return false;
        }

        // Check if OTP matches
        if (!smsOtp.getOtp().equals(otp)) {
            logger.debug("OTP mismatch for phone: '{}'. Expected: '{}', Got: '{}'", phoneNumber, smsOtp.getOtp(), otp);
            return false;
        }

        // Delete the OTP for one-time use
        logger.debug("OTP validation successful for phone: '{}'. Deleting record.", phoneNumber);
        repo.deleteById(phoneNumber);

        return true;
    }
}
