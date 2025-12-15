package gr.hua.dit.my.city.gov.web.rest.dto;

/**
 * Response DTO for authentication endpoints.
 */
public class AuthResponse {
    private String token;

    public AuthResponse() {
    }

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
