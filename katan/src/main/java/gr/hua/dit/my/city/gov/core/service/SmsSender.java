package gr.hua.dit.my.city.gov.core.service;

import gr.hua.dit.my.city.gov.core.port.SmsNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for sending SMS messages to users.
 */
@Service
public class SmsSender {

    private static final Logger logger = LoggerFactory.getLogger(SmsSender.class);

    private final SmsNotificationPort smsNotificationPort;

    public SmsSender(SmsNotificationPort smsNotificationPort) {
        this.smsNotificationPort = smsNotificationPort;
    }

    /**
     * Send an SMS message to the specified phone number.
     *
     * @param phoneNumber the recipient's phone number in E.164 format
     * @param message     the message content to send
     * @return true if the SMS was sent successfully, false otherwise
     */
    public boolean sendSms(final String phoneNumber, final String message) {
        logger.debug("Attempting to send SMS to: {}", phoneNumber);

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.warn("Cannot send SMS: phone number is null or empty");
            return false;
        }

        if (message == null || message.trim().isEmpty()) {
            logger.warn("Cannot send SMS to {}: message is null or empty", phoneNumber);
            return false;
        }

        try {
            boolean result = smsNotificationPort.sendSms(phoneNumber, message);
            if (result) {
                logger.info("SMS sent successfully to: {}", phoneNumber);
            } else {
                logger.warn("Failed to send SMS to: {}", phoneNumber);
            }
            return result;
        } catch (Exception e) {
            logger.error("Error sending SMS to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }
}
