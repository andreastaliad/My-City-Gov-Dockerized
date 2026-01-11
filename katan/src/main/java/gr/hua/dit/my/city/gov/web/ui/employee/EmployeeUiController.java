package gr.hua.dit.my.city.gov.web.ui.employee;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import gr.hua.dit.my.city.gov.core.repository.AppointmentRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeUiController {

    private final CurrentUserProvider currentUserProvider;
    private final PersonRepository personRepository;
    private final RequestRepository requestRepository;
    private final AppointmentRepository appointmentRepository;

    public EmployeeUiController(CurrentUserProvider currentUserProvider,
                                PersonRepository personRepository,
                                RequestRepository requestRepository,
                                AppointmentRepository appointmentRepository) {
        this.currentUserProvider = currentUserProvider;
        this.personRepository = personRepository;
        this.requestRepository = requestRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/home")
    public String home() {
        return "employee/employee-home";
    }

    @GetMapping("/overview")
    public String overview(Model model) {
        Person employee = resolveCurrentEmployee();

        model.addAttribute("employeeName", employee.getEmailAddress()); // ή ό,τι έχεις
        model.addAttribute("serviceUnitName",
                employee.getServiceUnit() != null ? employee.getServiceUnit().getName() : "—");

        // απλή επισκόπηση (θα τη βελτιώσεις αργότερα)
        model.addAttribute("requestsCount", employee.getServiceUnit() == null ? 0 :
                requestRepository.countByRequestType_ServiceUnit_Id(employee.getServiceUnit().getId()));

        model.addAttribute("appointmentsCount", employee.getServiceUnit() == null ? 0 :
                appointmentRepository.countByServiceUnitId(employee.getServiceUnit().getId()));

        return "employee/employee-overview :: content";
    }

    private Person resolveCurrentEmployee() {
        Long id = currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));

        return personRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Person not found: " + id));
    }
}
