package gr.hua.dit.my.city.gov.web.ui;


import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.port.PhoneNumberPort;
import gr.hua.dit.my.city.gov.core.port.SmsNotificationPort;
import gr.hua.dit.my.city.gov.core.port.impl.dto.PhoneNumberValidationResult;
import gr.hua.dit.my.city.gov.core.service.EmailSender;
import gr.hua.dit.my.city.gov.core.service.OtpService;
import gr.hua.dit.my.city.gov.core.service.PersonService;

import gr.hua.dit.my.city.gov.core.service.model.CreatePersonRequest;

import gr.hua.dit.my.city.gov.core.service.model.CreatePersonResult;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * UI controller for managing citizen registration.
 */
@Controller
public class RegistrationController {

    private final PersonService personService;
    private final OtpService otpService;
    private final SmsNotificationPort smsNotificationPort;
    private final PhoneNumberPort phoneNumberPort;
    private final EmailSender emailSender;

    public RegistrationController(final PersonService personService, final OtpService otpService,
                                   final SmsNotificationPort smsNotificationPort, final PhoneNumberPort phoneNumberPort,
                                   final EmailSender emailSender) {
        if (personService == null) throw new NullPointerException();
        if (otpService == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();
        if (phoneNumberPort == null) throw new NullPointerException();
        if (emailSender == null) throw new NullPointerException();
        this.personService = personService;
        this.otpService = otpService;
        this.smsNotificationPort = smsNotificationPort;
        this.phoneNumberPort = phoneNumberPort;
        this.emailSender = emailSender;
    }

    @GetMapping("/register")
    public String showRegistrationForm(
        final Authentication authentication,
        final Model model
    ) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }
        // Initial data for the form.
        final CreatePersonRequest createPersonRequest = new CreatePersonRequest(PersonType.CITIZEN, "", "", "", "", "", "", "", "");
        model.addAttribute("createPersonRequest", createPersonRequest);
        return "register";
    }

    @PostMapping("/register")
    public String handleFormSubmission(
        final Authentication authentication,
        @ModelAttribute("createPersonRequest") final CreatePersonRequest createPersonRequest,
        final Model model,
        final HttpSession session
    ) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile"; // already logged in.
        }
        // TODO Form validation + UI errors.
        
        // Validate and normalize phone number first
        String rawPhoneNumber = createPersonRequest.phoneNumber();
        PhoneNumberValidationResult phoneValidation = phoneNumberPort.validate(rawPhoneNumber);
        
        if (!phoneValidation.isValidMobile()) {
            model.addAttribute("createPersonRequest", createPersonRequest);
            model.addAttribute("errorMessage", "Phone Number is not valid");
            return "register";
        }
        
        String normalizedPhoneNumber = phoneValidation.e164().trim();
        
        // Ensure type is set to CITIZEN (since it's not in the form anymore)
        final CreatePersonRequest requestWithType = new CreatePersonRequest(
            PersonType.CITIZEN,
            createPersonRequest.afm(),
            createPersonRequest.amka(),
            createPersonRequest.firstName(),
            createPersonRequest.lastName(),
            createPersonRequest.emailAddress(),
            normalizedPhoneNumber,  // Use normalized phone number
            createPersonRequest.passwordHash(),
            createPersonRequest.rawPassword()
        );
        
        // Perform basic validation checks (without database save)
        String errorMessage = validateRegistrationRequest(requestWithType);
        if (errorMessage != null) {
            model.addAttribute("createPersonRequest", createPersonRequest);
            model.addAttribute("errorMessage", errorMessage);
            return "register";
        }
        
        // Validation passed - store the registration data in session
        String emailAddress = requestWithType.emailAddress();
        
        // Generate and send OTP (using normalized phone number)
        otpService.generateOtp(normalizedPhoneNumber);
        
        // Store registration data in session for later creation after OTP verification
        session.setAttribute("registrationPhoneNumber", normalizedPhoneNumber);
        session.setAttribute("registrationEmail", emailAddress);
        session.setAttribute("registrationRequest", requestWithType);
        
        return "redirect:/register/verify-otp"; // Redirect to OTP verification
    }
    
    /**
     * Validate registration request data WITHOUT saving to database.
     * @return error message if validation fails, null if validation passes
     */
    private String validateRegistrationRequest(final CreatePersonRequest request) {
        // Basic validation - PersonService will do full validation when we actually create
        if (request.firstName() == null || request.firstName().isBlank()) {
            return "First name is required";
        }
        if (request.lastName() == null || request.lastName().isBlank()) {
            return "Last name is required";
        }
        if (request.emailAddress() == null || request.emailAddress().isBlank()) {
            return "Email address is required";
        }
        if (!request.emailAddress().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Please provide a valid email address";
        }
        if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
            return "Phone number is required";
        }
        if (request.afm() == null || request.afm().isBlank()) {
            return "AFM is required";
        }
        if (request.amka() == null || request.amka().isBlank()) {
            return "AMKA is required";
        }
        if (request.rawPassword() == null || request.rawPassword().isBlank()) {
            return "Password is required";
        }
        
        return null; // All basic validations passed
    }

    @GetMapping("/register/verify-otp")
    public String showOtpVerificationForm(
        final Authentication authentication,
        final HttpSession session,
        final Model model
    ) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }
        
        String phoneNumber = (String) session.getAttribute("registrationPhoneNumber");
        if (phoneNumber == null) {
            return "redirect:/register"; // if there is no number restart
        }
        
        model.addAttribute("phoneNumber", phoneNumber);
        return "register-verify-otp";
    }

    @PostMapping("/register/verify-otp")
    public String handleOtpVerification(
        final Authentication authentication,
        final HttpSession session,
        final Model model
    ) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }
        
        String phoneNumber = (String) session.getAttribute("registrationPhoneNumber");
        String emailAddress = (String) session.getAttribute("registrationEmail");
        CreatePersonRequest registrationRequest = (CreatePersonRequest) session.getAttribute("registrationRequest");
        
        if (phoneNumber == null || registrationRequest == null) {
            model.addAttribute("errorMessage", "Registration session expired. Please start again.");
            return "redirect:/register";
        }
        
        // Check if OTP was verified
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        String otpVerifiedPhone = (String) session.getAttribute("otpVerifiedPhone");
        
        if (otpVerified == null || !otpVerified || !phoneNumber.equals(otpVerifiedPhone)) {
            model.addAttribute("errorMessage", "Please verify your OTP first.");
            model.addAttribute("phoneNumber", phoneNumber);
            return "register-verify-otp";
        }
        
        // OTP was verified - proceed with person creation
        final CreatePersonResult finalResult = this.personService.createPerson(registrationRequest, false);
        
        if (finalResult.created()) {
            // Person successfully created! Send welcome SMS
            if (emailAddress != null) {
                final String content = String.format(
                    "You have successfully registered for My City Gov. Use your email (%s) to log in.", 
                    emailAddress);
                this.smsNotificationPort.sendSms(phoneNumber, content);

                // Also send a simple confirmation email via Nylas
                this.emailSender.sendAccountCreatedEmail(emailAddress);
            }
            
            // Clear session - user can now log in
            session.removeAttribute("registrationPhoneNumber");
            session.removeAttribute("registrationEmail");
            session.removeAttribute("registrationRequest");
            session.removeAttribute("otpVerified");
            session.removeAttribute("otpVerifiedPhone");
            
            return "redirect:/login";
        } else {
            // Registration creation failed
            model.addAttribute("errorMessage", finalResult.reason());
            model.addAttribute("phoneNumber", phoneNumber);
            return "register-verify-otp";
        }
    }
}
