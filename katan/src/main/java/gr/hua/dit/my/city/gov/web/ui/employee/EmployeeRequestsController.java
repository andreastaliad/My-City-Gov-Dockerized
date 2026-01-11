package gr.hua.dit.my.city.gov.web.ui.employee;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.model.Request;
import gr.hua.dit.my.city.gov.core.model.RequestStatus;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import gr.hua.dit.my.city.gov.core.service.EmailSender;
import gr.hua.dit.my.city.gov.core.service.SmsSender;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employee/requests")
public class EmployeeRequestsController {

    private final RequestRepository requestRepository;
    private final PersonRepository personRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SmsSender smsSender;
    private final EmailSender emailSender;

    public EmployeeRequestsController(RequestRepository requestRepository,
                                      PersonRepository personRepository,
                                      CurrentUserProvider currentUserProvider,
                                      SmsSender smsSender,
                                      EmailSender emailSender) {
        this.requestRepository = requestRepository;
        this.personRepository = personRepository;
        this.currentUserProvider = currentUserProvider;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
    }

    @GetMapping("/employee/requests")
    public String myServiceRequests(Model model) {
        Long personId = currentUserProvider.getCurrentUser() .map(CurrentUser::id) .orElse(null);

        if (personId == null) { model.addAttribute("requests", List.of()); model.addAttribute("error", "Δεν βρέθηκε συνδεδεμένος χρήστης.");
            return "employee/employee-requests-list :: content"; }

        Person employee = personRepository.findById(personId) .orElseThrow(() -> new IllegalStateException("Person not found: " + personId));
        // Προαιρετικό: επιβεβαίωση ότι είναι EMPLOYEE
        if (employee.getType() != PersonType.EMPLOYEE) { model.addAttribute("requests", List.of()); model.addAttribute("error", "Μόνο υπάλληλοι έχουν πρόσβαση σε αυτή τη σελίδα.");
            return "employee/employee-requests-list :: content"; }

        if (employee.getServiceUnit() == null) { model.addAttribute("requests", List.of()); model.addAttribute("error", "Ο υπάλληλος δεν είναι αντιστοιχισμένος σε υπηρεσία.");
            return "employee/employee-requests-list :: content"; }

        Long suId = employee.getServiceUnit().getId(); model.addAttribute("requests",
                requestRepository.findByRequestType_ServiceUnit_IdOrderByCreatedAtDesc(suId));
        return "employee/employee-requests-list :: content"; }

    @GetMapping
    public String listRequests(Model model) {
        List<Request> requests = requestRepository.findAll();
        model.addAttribute("requests", requests);
        return "employee/employee-requests-list";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam RequestStatus status) {

        Request request = requestRepository.findById(id).orElseThrow();
        request.setStatus(status);

        if (status == RequestStatus.COMPLETED || status == RequestStatus.CLOSED) {
            if (request.getCompletedAt() == null) {
                request.setCompletedAt(LocalDateTime.now());
            }
        }

        request = requestRepository.save(request);

        Person citizen = request.getCitizen();
        if (citizen != null) {
            String message = String.format(
                    "Your request '%s' (protocol %s) status is now: %s",
                    request.getTitle(),
                    request.getProtocolNumber(),
                    request.getStatus().name()
            );

            if (citizen.getPhoneNumber() != null && !citizen.getPhoneNumber().isBlank()) {
                smsSender.sendSms(citizen.getPhoneNumber(), message);
            }

            if (citizen.getEmailAddress() != null && !citizen.getEmailAddress().isBlank()) {
                emailSender.sendSimpleEmail(
                        citizen.getEmailAddress(),
                        "My City Gov - Request status updated",
                        message
                );
            }
        }

        return "redirect:/employee/requests";
    }
}
