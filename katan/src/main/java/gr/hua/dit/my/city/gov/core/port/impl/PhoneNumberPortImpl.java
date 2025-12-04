package gr.hua.dit.my.city.gov.core.port.impl;

import gr.hua.dit.my.city.gov.config.RestApiClientConfig;
import gr.hua.dit.my.city.gov.core.port.PhoneNumberPort;
import gr.hua.dit.my.city.gov.core.port.impl.dto.PhoneNumberValidationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Default implementation of {@link PhoneNumberPort}. It uses the NOC external service.
 */
@Service
public class PhoneNumberPortImpl implements PhoneNumberPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberPortImpl.class);

    private final RestTemplate restTemplate;

    public PhoneNumberPortImpl(final RestTemplate restTemplate) {
        if (restTemplate == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
    }

    @Override
    public PhoneNumberValidationResult validate(final String rawPhoneNumber) {
        if (rawPhoneNumber == null) throw new NullPointerException();
        if (rawPhoneNumber.isBlank()) throw new IllegalArgumentException();

        // HTTP Request
        // --------------------------------------------------

        final String baseUrl = RestApiClientConfig.BASE_URL;
        final String url = baseUrl + "/api/v1/phone-numbers/" + rawPhoneNumber + "/validations";

        try {
            final ResponseEntity<PhoneNumberValidationResult> response
                = this.restTemplate.getForEntity(url, PhoneNumberValidationResult.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                final PhoneNumberValidationResult phoneNumberValidationResult = response.getBody();
                if (phoneNumberValidationResult == null) throw new NullPointerException();
                return phoneNumberValidationResult;
            }

            throw new RuntimeException("External service responded with " + response.getStatusCode());
        } catch (final RestClientException ex) {
            LOGGER.warn("Phone number validation failed (service unavailable or network error) for {}: {}",
                rawPhoneNumber, ex.toString());
            // Degrade gracefully: return an invalid result so callers can handle it as "not valid".
            return new PhoneNumberValidationResult(rawPhoneNumber, false, null, null);
        }
    }
}
