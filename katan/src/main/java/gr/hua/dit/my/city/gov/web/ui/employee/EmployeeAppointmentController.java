package gr.hua.dit.my.city.gov.web.ui.employee;

import gr.hua.dit.my.city.gov.core.model.Appointment;
import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.repository.AppointmentRepository;
import gr.hua.dit.my.city.gov.core.model.AppointmentStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

//Controller υπεύθυνο για την διαχείριση ραντεβού στην υπηρεσία του υπαλλήλου

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

    private void loadAppointments(Model model, Long suId) {
        model.addAttribute("appointments",
                appointmentRepository.findByServiceUnitIdOrderByDateDescTimeDesc(suId)
        );
    }

    private Person resolveEmployeeOrError(Model model) {

        Long personId = currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .orElse(null);

        if (personId == null) {
            model.addAttribute("appointments", List.of());
            model.addAttribute("error", "Δεν βρέθηκε συνδεδεμένος χρήστης.");
            return null;
        }

        Person employee = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalStateException("Person not found: " + personId));

        if (employee.getType() != PersonType.EMPLOYEE) {
            model.addAttribute("appointments", List.of());
            model.addAttribute("error", "Μόνο υπάλληλοι έχουν πρόσβαση.");
            return null;
        }

        if (employee.getServiceUnit() == null) {
            model.addAttribute("appointments", List.of());
            model.addAttribute("error", "Ο υπάλληλος δεν είναι αντιστοιχισμένος σε υπηρεσία.");
            return null;
        }

        return employee;
    }

    //λίστα ραντεβού υπηρεσίας
    @GetMapping
    public String listAppointments(Model model) {
        Person employee = resolveEmployeeOrError(model);
        if (employee == null) {
            return "employee/employee-appointments-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();
        loadAppointments(model, suId);

        return "employee/employee-appointments-list :: content";
    }

    //επιβεβαίωση
    @PostMapping("/{id}/confirm")
    @Transactional
    public String confirm(@PathVariable Long id, Model model) {
        Person employee = resolveEmployeeOrError(model);
        if (employee == null) {
            return "employee/employee-appointments-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();

        int updated = appointmentRepository.confirmIfScheduled(id, suId);
        if (updated == 0) {
            model.addAttribute("error", "Το ραντεβού δεν είναι πλέον σε κατάσταση SCHEDULED ή δεν ανήκει στην υπηρεσία σας.");
        } else {
            model.addAttribute("success", "Το ραντεβού επιβεβαιώθηκε.");
        }

        loadAppointments(model, suId);
        return "employee/employee-appointments-list :: content";
    }

    //αλλαγή ώρας
    @PostMapping("/{id}/reschedule")
    @Transactional
    public String reschedule(@PathVariable Long id,
                             @RequestParam("newDate") LocalDate newDate,
                             @RequestParam("newTime") LocalTime newTime,
                             @RequestParam(value = "note", required = false) String note,
                             Model model) {

        Person employee = resolveEmployeeOrError(model);
        if (employee == null) {
            return "employee/employee-appointments-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();

        // Basic validation
        if (newDate == null || newTime == null) {
            model.addAttribute("error", "Απαιτείται νέα ημερομηνία και ώρα.");
            loadAppointments(model, suId);
            return "employee/employee-appointments-list :: content";
        }

        String cleanNote = (note == null || note.isBlank()) ? null : note.trim();

        int updated = appointmentRepository.rescheduleIfNotCancelled(id, suId, newDate, newTime, cleanNote);
        if (updated == 0) {
            model.addAttribute("error", "Δεν είναι δυνατή η αλλαγή ώρας (ίσως ακυρωμένο ή άλλης υπηρεσίας).");
        } else {
            model.addAttribute("success", "Το ραντεβού μεταφέρθηκε.");
        }

        loadAppointments(model, suId);
        return "employee/employee-appointments-list :: content";
    }

    //ακύρωση
    @PostMapping("/{id}/cancel")
    @Transactional
    public String cancel(@PathVariable Long id,
                         @RequestParam(value = "reason", required = false) String reason,
                         Model model) {

        Person employee = resolveEmployeeOrError(model);
        if (employee == null) {
            return "employee/employee-appointments-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();
        String cleanReason = (reason == null || reason.isBlank()) ? null : reason.trim();

        int updated = appointmentRepository.cancelIfNotCancelled(id, suId, cleanReason);
        if (updated == 0) {
            model.addAttribute("error", "Το ραντεβού είναι ήδη ακυρωμένο ή δεν ανήκει στην υπηρεσία σας.");
        } else {
            model.addAttribute("success", "Το ραντεβού ακυρώθηκε.");
        }

        loadAppointments(model, suId);
        return "employee/employee-appointments-list :: content";
    }

}
