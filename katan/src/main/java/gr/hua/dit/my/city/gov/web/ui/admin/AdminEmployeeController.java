package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.service.model.EmployeeRegistrationService;
import gr.hua.dit.my.city.gov.web.rest.dto.EmployeeCreateRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String createEmployee(EmployeeCreateRequest request) {
        employeeRegistrationService.registerEmployee(request);
        return "admin/employee-success :: content";
    }

}
