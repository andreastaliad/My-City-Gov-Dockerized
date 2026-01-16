package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.model.*;
import gr.hua.dit.my.city.gov.core.repository.AppointmentRepository;
import gr.hua.dit.my.city.gov.core.repository.IssueRepository;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import gr.hua.dit.my.city.gov.core.service.model.AdminUserService;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.*;

//Controller υπεύθυνος για την βασική ui λειτουργία του admin
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUiController {

    private final AdminUserService adminUserService;
    private final RequestRepository requestRepository;
    private final AppointmentRepository appointmentRepository;
    private final IssueRepository issueRepository;
    private final PersonRepository personRepository;

    public AdminUiController(AdminUserService adminUserService, RequestRepository requestRepository, AppointmentRepository appointmentRepository, IssueRepository issueRepository, PersonRepository personRepository) {
        this.adminUserService = adminUserService;
        this.requestRepository = requestRepository;
        this.appointmentRepository = appointmentRepository;
        this.issueRepository = issueRepository;
        this.personRepository = personRepository;
    }

    //admin dashboard
    @GetMapping("/home")
    public String home() {
        return "admin/admin-home";
    }

    //menu επιλογης
    @GetMapping("/users/menu")
    public String usersMenu() {
        return "admin/users-menu :: content";
    }

    //πίνακας υπαλλήλων
    @GetMapping("/users/employees")
    public String employees(Model model) {
        model.addAttribute("employees",
                adminUserService.getAllEmployees());
        return "admin/employees-list :: content";
    }

    //πίνακας πολιτών
    @GetMapping("/users/citizens")
    public String citizens(Model model) {
        model.addAttribute("citizens",
                adminUserService.getAllCitizens());
        return "admin/citizens-list :: content";
    }

    //πίνακας αιτημάτων
    @GetMapping("/requests")
    public String adminRequests(Model model) {

        List<Request> requests = requestRepository.findAll();
        model.addAttribute("requests", requests);

        LocalDateTime now = LocalDateTime.now();
        //Αν το αίτημα μείνει σε συγκεκριμένη κατάσταση πάνω από Χ ημέρες
        int overdueDays = 7; //εδώ μπαίνει το Χ

        //serviceUnitId -> employees
        Map<Long, List<Person>> employeesByServiceUnit = new LinkedHashMap<>();

        for (Request r : requests) {
            if (r.getRequestType() == null || r.getRequestType().getServiceUnit() == null) continue;

            Long suId = r.getRequestType().getServiceUnit().getId();

            employeesByServiceUnit.computeIfAbsent(suId, k ->
                    personRepository.findByTypeAndServiceUnit_IdOrderByEmailAddressAsc(PersonType.EMPLOYEE, suId)
            );
        }

        Set<Long> overdueIds = requests.stream()
                //αγνοεί ολοκληρωμένα
                .filter(r -> r.getStatus() != RequestStatus.COMPLETED)
                .filter(r -> r.getStatus() != RequestStatus.CLOSED)

                //ελέγχει την απόφαση του υπαλλήλου
                .filter(r ->
                        r.getEmployeeDecision() == null
                                || r.getEmployeeDecision() == EmployeeDecision.PENDING
                )

                //εύρεση του πότε έμεινε
                .filter(r -> {
                    LocalDateTime base =
                            r.getAssignedAt() != null
                                    ? r.getAssignedAt()
                                    : r.getCreatedAt();

                    return base != null &&
                            base.isBefore(LocalDateTime.now().minusDays(overdueDays));
                })

                .map(Request::getId)
                .collect(Collectors.toSet());

        model.addAttribute("employeesByServiceUnit", employeesByServiceUnit);
        model.addAttribute("overdueIds", overdueIds);
        model.addAttribute("overdueDays", overdueDays);

        return "admin/requests-list :: content";
    }


    //πίνακας ραντεβού
    @GetMapping("/appointments")
    public String adminAppointments(Model model) {
        model.addAttribute("appointments", appointmentRepository.findAll());
        return "admin/appointments-list :: content";
    }

    //admin overview
    @GetMapping("/overview")
    public String overview(Model model) {
        model.addAttribute("citizenCount",
                adminUserService.getAllCitizens().size());

        model.addAttribute("employeeCount",
                adminUserService.getAllEmployees().size());

        model.addAttribute("requestCount",
                requestRepository.count());

        model.addAttribute("appointmentCount",
                appointmentRepository.count());

        model.addAttribute("issuesCount",
                issueRepository.count());

        return "admin/admin-overview :: content";
    }

    //φόρμα δημιουργίας υπαλλήλου
    @GetMapping("/employees/new")
    public String newEmployee() {
        return "admin/employee-create-form :: content";
    }
}
