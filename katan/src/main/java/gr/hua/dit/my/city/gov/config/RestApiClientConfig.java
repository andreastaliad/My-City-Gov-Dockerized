package gr.hua.dit.my.city.gov.config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestApiClientConfig {

    // Initialized from application properties (fallback to localhost)
    public static String BASE_URL = "http://localhost:8081";

    @Value("${external.api.base-url:http://localhost:8081}")
    private String configuredBaseUrl;

    @PostConstruct
    public void init() {
        BASE_URL = this.configuredBaseUrl;
    }

    @Bean
    public RestTemplate restTemplate() {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // Increase timeouts to avoid Read timed out for slower external services (e.g. SMS provider).
        factory.setConnectTimeout(5_000); // 5 seconds
        factory.setReadTimeout(10_000); // 10 seconds
        return new RestTemplate(factory);
    }
}
