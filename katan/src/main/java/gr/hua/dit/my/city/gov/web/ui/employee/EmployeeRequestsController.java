package gr.hua.dit.my.city.gov.web.ui.employee;

import gr.hua.dit.my.city.gov.core.model.*;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.repository.RequestCommentRepository;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import gr.hua.dit.my.city.gov.core.service.EmailSender;
import gr.hua.dit.my.city.gov.core.service.SmsSender;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employee/requests")
public class EmployeeRequestsController {

    private final RequestRepository requestRepository;
    private final PersonRepository personRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SmsSender smsSender;
    private final EmailSender emailSender;
    private final RequestCommentRepository requestCommentRepository;

    private void loadRequests(Model model, Long serviceUnitId, Long employeeId) {

        List<Request> unassigned = requestRepository
                .findByRequestType_ServiceUnit_IdAndAssignedEmployeeIsNullOrderByCreatedAtDesc(serviceUnitId);

        List<Request> mine = requestRepository
                .findByRequestType_ServiceUnit_IdAndAssignedEmployee_IdOrderByCreatedAtDesc(serviceUnitId, employeeId);

        // merge χωρίς διπλότυπα
        Map<Long, Request> map = new LinkedHashMap<>();
        for (Request r : unassigned) map.put(r.getId(), r);
        for (Request r : mine) map.put(r.getId(), r);

        List<Request> reqs = new ArrayList<>(map.values());

        model.addAttribute("requests", reqs);
        model.addAttribute("employeeId", employeeId);

        // σχόλια ανά αίτημα (χρησιμοποιεί την υπάρχουσα μέθοδο του repo)
        Map<Long, List<RequestComment>> commentsByRequestId = new LinkedHashMap<>();
        for (Request r : reqs) {
            commentsByRequestId.put(
                    r.getId(),
                    requestCommentRepository.findByRequest_IdOrderByCreatedAtDesc(r.getId())
            );
        }
        model.addAttribute("commentsByRequestId", commentsByRequestId);
    }


    public EmployeeRequestsController(RequestRepository requestRepository,
                                      PersonRepository personRepository,
                                      RequestCommentRepository requestCommentRepository,
                                      CurrentUserProvider currentUserProvider,
                                      SmsSender smsSender,
                                      EmailSender emailSender) {
        this.requestRepository = requestRepository;
        this.personRepository = personRepository;
        this.requestCommentRepository = requestCommentRepository;
        this.currentUserProvider = currentUserProvider;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
    }

    @GetMapping
    public String listMyServiceRequests(Model model) {

        Long personId = currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .orElseThrow();

        Person employee = personRepository.findById(personId).orElseThrow();

        if (employee.getServiceUnit() == null) {
            model.addAttribute("requests", List.of());
            model.addAttribute("error", "Δεν είστε αντιστοιχισμένος σε υπηρεσία");
            return "employee/employee-requests-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();

        // Προτείνω να βλέπει: unassigned + τα δικά του
        List<Request> unassigned = requestRepository
                .findByRequestType_ServiceUnit_IdAndAssignedEmployeeIsNullOrderByCreatedAtDesc(suId);

        List<Request> mine = requestRepository
                .findByRequestType_ServiceUnit_IdAndAssignedEmployee_IdOrderByCreatedAtDesc(suId, employee.getId());

        // merge χωρίς διπλότυπα
        Map<Long, Request> map = new LinkedHashMap<>();
        for (Request r : unassigned) map.put(r.getId(), r);
        for (Request r : mine) map.put(r.getId(), r);

        List<Request> reqs = new ArrayList<>(map.values());

        model.addAttribute("requests", reqs);
        model.addAttribute("employeeId", employee.getId());

        // ADDITIVE: φόρτωσε σχόλια για όσα requests ήδη εμφανίζεις
        Map<Long, List<RequestComment>> commentsByRequestId = new LinkedHashMap<>();
        for (Request r : reqs) {
            commentsByRequestId.put(
                    r.getId(),
                    requestCommentRepository.findByRequest_IdOrderByCreatedAtDesc(r.getId())
            );
        }

        model.addAttribute("commentsByRequestId", commentsByRequestId);
        model.addAttribute("employeeId", employee.getId());

        return "employee/employee-requests-list :: content";
    }

    @PostMapping("/{id}/claim")
    @Transactional
    public String claim(@PathVariable Long id, Model model) {

        Long personId = currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .orElseThrow();

        Person employee = personRepository.findById(personId).orElseThrow();

        if (employee.getServiceUnit() == null) {
            model.addAttribute("requests", List.of());
            model.addAttribute("employeeId", employee.getId());
            model.addAttribute("error", "Δεν είστε αντιστοιχισμένος σε υπηρεσία");
            return "employee/employee-requests-list :: content";
        }

        Request request = requestRepository.findById(id).orElseThrow();

        Long suId = employee.getServiceUnit().getId();
        if (request.getRequestType() == null
                || request.getRequestType().getServiceUnit() == null
                || !request.getRequestType().getServiceUnit().getId().equals(suId)) {

            loadRequests(model, suId, employee.getId());
            model.addAttribute("error", "Δεν επιτρέπεται ανάληψη αιτήματος άλλης υπηρεσίας.");
            return "employee/employee-requests-list :: content";
        }

        // Atomic claim (όπως το έχεις)
        requestRepository.claimIfUnassigned(id, employee.getId(), LocalDateTime.now());

        loadRequests(model, suId, employee.getId());
        return "employee/employee-requests-list :: content";
    }


    @PostMapping("/{id}/unclaim")
    @Transactional
    public String unclaim(@PathVariable Long id, Model model) {

        Long personId = currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .orElseThrow();

        Person employee = personRepository.findById(personId).orElseThrow();

        if (employee.getServiceUnit() == null) {
            model.addAttribute("requests", List.of());
            model.addAttribute("employeeId", employee.getId());
            model.addAttribute("error", "Δεν είστε αντιστοιχισμένος σε υπηρεσία");
            return "employee/employee-requests-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();

        // Μόνο αν είναι δικό του (όπως το έχεις)
        requestRepository.unclaimIfOwned(id, personId);

        loadRequests(model, suId, employee.getId());
        return "employee/employee-requests-list :: content";
    }

    @PostMapping("/{id}/status")
    @Transactional
    public String updateStatus(@PathVariable Long id,
                               @RequestParam RequestStatus status,
                               Model model) {

        Long personId = currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .orElseThrow();

        Person employee = personRepository.findById(personId).orElseThrow();

        if (employee.getServiceUnit() == null) {
            model.addAttribute("requests", List.of());
            model.addAttribute("employeeId", employee.getId());
            model.addAttribute("error", "Δεν είστε αντιστοιχισμένος σε υπηρεσία");
            return "employee/employee-requests-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();
        Request request = requestRepository.findById(id).orElseThrow();

        // Security 1: ίδιο service unit
        if (request.getRequestType() == null
                || request.getRequestType().getServiceUnit() == null
                || !request.getRequestType().getServiceUnit().getId().equals(suId)) {

            loadRequests(model, suId, employee.getId());
            model.addAttribute("error", "Δεν επιτρέπεται να ενημερώσετε αίτημα άλλης υπηρεσίας.");
            return "employee/employee-requests-list :: content";
        }

        // Security 2: update μόνο αν είναι ανατεθειμένο στον ίδιο
        if (request.getAssignedEmployee() == null
                || !request.getAssignedEmployee().getId().equals(employee.getId())) {

            loadRequests(model, suId, employee.getId());
            model.addAttribute("error", "Δεν επιτρέπεται να ενημερώσετε αίτημα που δεν είναι ανατεθειμένο σε εσάς.");
            return "employee/employee-requests-list :: content";
        }

        request.setStatus(status);

        if (status == RequestStatus.COMPLETED || status == RequestStatus.CLOSED) {
            if (request.getCompletedAt() == null) {
                request.setCompletedAt(LocalDateTime.now());
            }
        }

        // Fix για DB constraint: due_at NOT NULL
        if (request.getDueAt() == null) {
            request.setDueAt(LocalDateTime.now().plusDays(7));
        }

        request = requestRepository.save(request);

        // Notifications (όπως το έχεις)
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

        loadRequests(model, suId, employee.getId());
        return "employee/employee-requests-list :: content";
    }

    @PostMapping("/{id}/comment")
    @Transactional
    public String addComment(@PathVariable Long id,
                             @RequestParam("text") String text,
                             Model model) {

        Long personId = currentUserProvider.getCurrentUser().map(CurrentUser::id).orElseThrow();
        Person employee = personRepository.findById(personId).orElseThrow();

        if (employee.getServiceUnit() == null) {
            model.addAttribute("requests", List.of());
            model.addAttribute("employeeId", employee.getId());
            model.addAttribute("error", "Δεν είστε αντιστοιχισμένος σε υπηρεσία");
            return "employee/employee-requests-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();
        Request request = requestRepository.findById(id).orElseThrow();

        // Security: ίδιο service unit
        if (request.getRequestType() == null
                || request.getRequestType().getServiceUnit() == null
                || !request.getRequestType().getServiceUnit().getId().equals(suId)) {
            loadRequests(model, suId, employee.getId());
            model.addAttribute("error", "Δεν επιτρέπεται σχόλιο σε αίτημα άλλης υπηρεσίας.");
            return "employee/employee-requests-list :: content";
        }

        // Αν θες: μόνο αν είναι assigned σε αυτόν
        if (request.getAssignedEmployee() == null
                || !request.getAssignedEmployee().getId().equals(employee.getId())) {
            loadRequests(model, suId, employee.getId());
            model.addAttribute("error", "Δεν μπορείτε να σχολιάσετε αίτημα που δεν είναι ανατεθειμένο σε εσάς.");
            return "employee/employee-requests-list :: content";
        }

        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) {
            loadRequests(model, suId, employee.getId());
            model.addAttribute("error", "Το σχόλιο δεν μπορεί να είναι κενό.");
            return "employee/employee-requests-list :: content";
        }

        RequestComment c = new RequestComment();
        c.setRequest(request);
        c.setAuthorEmployee(employee);
        c.setText(clean);
        requestCommentRepository.save(c);

        loadRequests(model, suId, employee.getId());
        return "employee/employee-requests-list :: content";
    }

    @PostMapping("/{id}/decision")
    @Transactional
    public String decide(@PathVariable Long id,
                         @RequestParam("decision") EmployeeDecision decision,
                         @RequestParam("reason") String reason,
                         Model model) {

        Long personId = currentUserProvider.getCurrentUser().map(CurrentUser::id).orElseThrow();
        Person employee = personRepository.findById(personId).orElseThrow();

        if (employee.getServiceUnit() == null) {
            model.addAttribute("requests", List.of());
            model.addAttribute("employeeId", employee.getId());
            model.addAttribute("error", "Δεν είστε αντιστοιχισμένος σε υπηρεσία");
            return "employee/employee-requests-list :: content";
        }

        Long suId = employee.getServiceUnit().getId();
        Request request = requestRepository.findById(id).orElseThrow();

        // Security 1: ίδιο service unit
        if (request.getRequestType() == null
                || request.getRequestType().getServiceUnit() == null
                || !request.getRequestType().getServiceUnit().getId().equals(suId)) {
            loadRequests(model, suId, employee.getId());
            model.addAttribute("error", "Δεν επιτρέπεται απόφαση σε αίτημα άλλης υπηρεσίας.");
            return "employee/employee-requests-list :: content";
        }

        // Security 2: μόνο ο assigned employee
        if (request.getAssignedEmployee() == null
                || !request.getAssignedEmployee().getId().equals(employee.getId())) {
            loadRequests(model, suId, employee.getId());
            model.addAttribute("error", "Δεν μπορείτε να εγκρίνετε/απορρίψετε αίτημα που δεν είναι ανατεθειμένο σε εσάς.");
            return "employee/employee-requests-list :: content";
        }

        // Validation: τεκμηρίωση υποχρεωτική ειδικά σε REJECTED
        String cleanReason = (reason == null) ? "" : reason.trim();
        if (decision == EmployeeDecision.REJECTED && cleanReason.isEmpty()) {
            loadRequests(model, suId, employee.getId());
            model.addAttribute("error", "Η απόρριψη απαιτεί τεκμηρίωση.");
            return "employee/employee-requests-list :: content";
        }

        // Update μόνο των πεδίων decision (ΟΧΙ save όλο το entity)
        LocalDateTime now = LocalDateTime.now();
        String reasonToStore = cleanReason.isEmpty() ? null : cleanReason;

        if (decision == EmployeeDecision.REJECTED) {
            requestRepository.updateDecisionAndUnassign(id, decision, reasonToStore, now);
        } else {
            // APPROVED (ή PENDING αν το επιτρέπεις): απλό update decision
            requestRepository.updateDecision(id, decision, reasonToStore, now);
        }

        loadRequests(model, suId, employee.getId());
        return "employee/employee-requests-list :: content";
    }

}
