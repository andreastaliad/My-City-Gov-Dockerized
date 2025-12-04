package gr.hua.dit.my.city.gov.core.port.impl;

import gr.hua.dit.my.city.gov.config.RestApiClientConfig;
import gr.hua.dit.my.city.gov.core.port.SmsNotificationPort;

import gr.hua.dit.my.city.gov.core.port.impl.dto.SendSmsRequest;

import gr.hua.dit.my.city.gov.core.port.impl.dto.SendSmsResult;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;



/**
 * Default implementation of {@link SmsNotificationPort}. It uses the NOC external service.
 */
@Service
public class SmsNotificationPortImpl implements SmsNotificationPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsNotificationPortImpl.class);

    private final RestTemplate restTemplate;

    public SmsNotificationPortImpl(final RestTemplate restTemplate) {
        if (restTemplate == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean sendSms(final String e164, final String content) {
        if (e164 == null) throw new NullPointerException();
        if (e164.isBlank()) throw new IllegalArgumentException();
        if (content == null) throw new NullPointerException();
        if (content.isBlank()) throw new IllegalArgumentException();

        // Headers
        // --------------------------------------------------

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Payload
        // --------------------------------------------------

        final SendSmsRequest body = new SendSmsRequest(e164, content);

        // Alternative: (Spring speaks JSON!!! Search for ObjectMapper!)
        // final Map<String, Object> body = Map.of("e164", e164, "body", content);

        // HTTP Request
        // --------------------------------------------------

        final String baseUrl = RestApiClientConfig.BASE_URL;
        final String url = baseUrl + "/api/v1/sms";
        final HttpEntity<SendSmsRequest> entity = new HttpEntity<>(body, httpHeaders);
        try {
            final ResponseEntity<SendSmsResult> response = this.restTemplate.postForEntity(url, entity, SendSmsResult.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                final SendSmsResult sendSmsResult = response.getBody();
                if (sendSmsResult == null) throw new NullPointerException();
                return sendSmsResult.sent();
            }
            //simple logging for debugging only
            LOGGER.warn("SMS provider responded with non-2xx status: {}", response.getStatusCode());
            return false;
        } catch (final RestClientException ex) {
            // Don't fail the registration flow on SMS send problems; log and degrade gracefully.
            LOGGER.warn("SMS send failed for {}: {}", e164, ex.toString());
            return false;
        }
    }
}
