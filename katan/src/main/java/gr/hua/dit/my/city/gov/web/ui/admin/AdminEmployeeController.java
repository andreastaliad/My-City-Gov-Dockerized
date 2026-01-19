package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.service.model.EmployeeRegistrationService;
import gr.hua.dit.my.city.gov.web.rest.dto.EmployeeCreateRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//Controller υπεύθυνο για την εγγραφή υπαλλήλων

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmployeeController {
    private final EmployeeRegistrationService employeeRegistrationService;

    public AdminEmployeeController(EmployeeRegistrationService employeeRegistrationService) {
        this.employeeRegistrationService = employeeRegistrationService;
    }

    //Κάνει την εγγραφή
    @PostMapping("/employees")
    public Object createEmployee(EmployeeCreateRequest request, HttpServletRequest httpRequest) {
        final boolean isAjax =
                "XMLHttpRequest".equalsIgnoreCase(httpRequest.getHeader("X-Requested-With"));

        try {
            employeeRegistrationService.registerEmployee(request);

            if (isAjax) {
                // Επιστρέφουμε απλό μήνυμα επιτυχίας για χρήση από AJAX
                return ResponseEntity.ok("OK");
            }

            // Μη-AJAX: απλό redirect, χωρίς λευκή σελίδα με κείμενο
            return "redirect:/admin/home";
        } catch (DataIntegrityViolationException ex) {
            // Πιθανή παραβίαση μοναδικότητας (AFM/AMKA/email κλπ.)
            if (isAjax) {
                return ResponseEntity.badRequest().body(
                        "Υπάρχει ήδη υπάλληλος με τα ίδια στοιχεία (π.χ. ΑΜΚΑ ή ΑΦΜ)."
                );
            }

            // Μη-AJAX: σε αποτυχία απλώς επιστρέφουμε στο admin home
            return "redirect:/admin/home";
        }
    }

}
