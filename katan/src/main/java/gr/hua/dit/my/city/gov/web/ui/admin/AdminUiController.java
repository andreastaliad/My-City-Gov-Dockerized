package gr.hua.dit.my.city.gov.web.ui.admin;

import org.springframework.ui.Model;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUiController {

    //admin dashboard
    @GetMapping("/home")
    public String home() {
        return "admin/admin-home";
    }

    //employee list
    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", List.of());
        return "admin/employees-list :: content";
    }

    //employee creation form
    @GetMapping("/employees/new")
    public String newEmployee() {
        return "admin/employee-create-form :: content";
    }
}
