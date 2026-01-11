package gr.hua.dit.my.city.gov.web.ui.employee;

import gr.hua.dit.my.city.gov.core.model.Appointment;
import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.repository.AppointmentRepository;

import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/employee/appointments")
public class EmployeeAppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final PersonRepository personRepository;
    private final CurrentUserProvider currentUserProvider;

    public EmployeeAppointmentController(AppointmentRepository appointmentRepository,
                                         PersonRepository personRepository,
                                         CurrentUserProvider currentUserProvider) {
        this.appointmentRepository = appointmentRepository;
        this.personRepository = personRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String listAppointments(Model model) {

        Long personId = currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .orElse(null);

        if (personId == null) {
            model.addAttribute("appointments", List.of());
            model.addAttribute("error", "Δεν βρέθηκε συνδεδεμένος χρήστης.");
            return "employee/employee-appointments-list :: content";
        }

        Person employee = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalStateException("Person not found: " + personId));

        if (employee.getType() != PersonType.EMPLOYEE) {
            model.addAttribute("appointments", List.of());
            model.addAttribute("error", "Μόνο υπάλληλοι έχουν πρόσβαση.");
            return "employee/employee-appointments-list :: content";
        }

        if (employee.getServiceUnit() == null) {
            model.addAttribute("appointments", List.of());
            model.addAttribute("error", "Ο υπάλληλος δεν είναι αντιστοιχισμένος σε υπηρεσία.");
            return "employee/employee-appointments-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();

        // ΕΔΩ βάλε το σωστό repository method (μία από τις 2 επιλογές)
        model.addAttribute("appointments",
                appointmentRepository.findByServiceUnitIdOrderByDateDescTimeDesc(suId)
        );

        return "employee/employee-appointments-list :: content";
    }
}
