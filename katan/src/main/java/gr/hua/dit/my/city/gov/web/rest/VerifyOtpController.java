package gr.hua.dit.my.city.gov.web.rest;

import gr.hua.dit.my.city.gov.core.service.OtpService;
import gr.hua.dit.my.city.gov.web.rest.dto.AuthResponse;
import gr.hua.dit.my.city.gov.web.rest.dto.OtpRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * REST Controller for OTP verification and authentication.
 */
@RestController
@RequestMapping("/auth")
public class VerifyOtpController {

    private static final Logger logger = LoggerFactory.getLogger(VerifyOtpController.class);
    private final OtpService otpService;

    public VerifyOtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * Verify OTP and authenticate user.
     *
     * @param request OTP request containing phone number and OTP
     * @param session HTTP session to store verification status
     * @return AuthResponse with authentication token if valid, or error response
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request, HttpSession session) {
        logger.debug("verifyOtp called with phone: '{}', otp: '{}'", request.getPhone(), request.getOtp());
        
        // Validate OTP
        boolean valid = otpService.validateOtp(request.getPhone(), request.getOtp());

        if (!valid) {
            logger.warn("OTP validation failed for phone: '{}'", request.getPhone());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired OTP");
        }

        // Mark OTP as verified in session for registration flow
        session.setAttribute("otpVerified", true);
        session.setAttribute("otpVerifiedPhone", request.getPhone());
        logger.debug("OTP verified successfully. Set session flag for phone: '{}'", request.getPhone());
        
        try {
            // Create authentication using phone number as principal
            Authentication auth = new Authentication() {
                @Override
                public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                }

                @Override
                public Object getCredentials() {
                    return null;
                }

                @Override
                public Object getDetails() {
                    return null;
                }

                @Override
                public Object getPrincipal() {
                    return request.getPhone();
                }

                @Override
                public boolean isAuthenticated() {
                    return true;
                }

                @Override
                public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                    // No-op
                }

                @Override
                public String getName() {
                    return request.getPhone();
                }
            };

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(auth);

            //TODO Generate JWT token using JwtService when available
            // For now, return a placeholder token
            String token = "Bearer_" + request.getPhone() + "_" + System.currentTimeMillis();

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed: " + e.getMessage());
        }
    }
}
