package gr.hua.dit.my.city.gov.web.rest;

import gr.hua.dit.my.city.gov.core.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String phoneNumber) {
        otpService.generateOtp(phoneNumber);
        return ResponseEntity.ok("OTP sent");
    }
}

