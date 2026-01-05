package gr.hua.dit.my.city.gov.core.service;

import com.nylas.NylasClient;
import com.nylas.models.EmailName;
import com.nylas.models.Message;
import com.nylas.models.Response;
import com.nylas.models.SendMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for sending notification emails to users via Nylas.
 */
@Service
public class EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    private final NylasClient nylasClient;
    private final String grantId;

    public EmailSender(
            @Value("${NYLAS_API_KEY}") final String apiKey,
            @Value("${NYLAS_API_URI}") final String apiUri,
            @Value("${NYLAS_GRANT_ID}") final String grantId
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("NYLAS_API_KEY is not configured");
        }
        if (apiUri == null || apiUri.isBlank()) {
            throw new IllegalStateException("NYLAS_API_URI is not configured");
        }
        if (grantId == null || grantId.isBlank()) {
            throw new IllegalStateException("NYLAS_GRANT_ID is not configured");
        }

        this.grantId = grantId;

        this.nylasClient = new NylasClient.Builder(apiKey)
                .apiUri(apiUri.endsWith("/") ? apiUri.substring(0, apiUri.length() - 1) : apiUri)
                .build();
    }

    /**
     * Sends a simple email informing the user that an account was created
     * with the specified email address.
     *
     * @param recipientEmail the user's email address
     */
    public void sendAccountCreatedEmail(final String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            logger.warn("Cannot send account created email: recipient email is null or blank");
            return;
        }

        try {
            final List<EmailName> recipients = new ArrayList<>();
            recipients.add(new EmailName(recipientEmail, null));

            final String subject = "My City Gov - Account created";
            final String body = String.format("You have successfully created a My City Gov account with this email address: %s.\n",recipientEmail);

            final SendMessageRequest requestBody = new SendMessageRequest.Builder(recipients)
                    .subject(subject)
                    .body(body)
                    .build();

            final Response<Message> response = this.nylasClient.messages().send(this.grantId, requestBody);

            if (response != null && response.getData() != null) {
                logger.info("Sent account created email to {} with message id {}", recipientEmail, response.getData().getId());
            } else {
                logger.info("Sent account created email to {} (no response details)", recipientEmail);
            }
        } catch (Exception e) {
            logger.error("Failed to send account created email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }
}
