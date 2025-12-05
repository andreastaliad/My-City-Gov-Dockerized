package gr.hua.dit.my.city.gov.web.ui;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UI controller for user authentication (login and logout).
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(
        final Authentication authentication,
        final HttpServletRequest request,
        final Model model
    ) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/home";
        }

        // Spring Security appends ?error or ?logout; show friendly messages.
        // TODO implement error message in front end
        if (request.getParameter("error") != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        if (request.getParameter("logout") != null) {
            model.addAttribute("message", "You have been logged out.");
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(final Authentication authentication) {
        if (AuthUtils.isAnonymous(authentication)) {
            return "redirect:/login";
        }
        return "logout";
    }
}
