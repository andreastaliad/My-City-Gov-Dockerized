package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.model.Request;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRequestsController {

    private final RequestRepository requestRepository;
    private final PersonRepository personRepository;

    public AdminRequestsController(RequestRepository requestRepository,
                                   PersonRepository personRepository) {
        this.requestRepository = requestRepository;
        this.personRepository = personRepository;
    }

    @PostMapping("/{id}/assign")
    @Transactional
    public String assign(@PathVariable Long id,
                         @RequestParam("employeeId") Long employeeId,
                         Model model) {

        Request request = requestRepository.findById(id).orElseThrow();
        Person employee = personRepository.findById(employeeId).orElseThrow();

        // Επιτρέπουμε μόνο υπάλληλο
        if (employee.getType() != PersonType.EMPLOYEE) {
            model.addAttribute("error", "Επιτράπηκε μόνο επιλογή υπαλλήλων.");
            return reloadAdminRequests(model);
        }

        // VALIDATION: ίδια υπηρεσία (service unit)
        Long reqSuId = (request.getRequestType() == null || request.getRequestType().getServiceUnit() == null)
                ? null
                : request.getRequestType().getServiceUnit().getId();

        Long empSuId = (employee.getServiceUnit() == null) ? null : employee.getServiceUnit().getId();

        if (reqSuId == null || empSuId == null || !reqSuId.equals(empSuId)) {
            model.addAttribute("error", "Δεν επιτρέπεται ανάθεση σε υπάλληλο άλλης υπηρεσίας.");
            return reloadAdminRequests(model);
        }

        // Αν είναι ήδη ανατεθειμένο στον ίδιο υπάλληλο δώσε μήνυμα
        if (request.getAssignedEmployee() != null
                && request.getAssignedEmployee().getId().equals(employeeId)) {
            model.addAttribute("error", "Το αίτημα είναι ήδη ανατεθειμένο στον συγκεκριμένο υπάλληλο.");
            return reloadAdminRequests(model);
        }


        // Ανάθεση (atomic update στο repo)
        int updated = requestRepository.adminAssignIfUnassigned(id, employeeId, LocalDateTime.now());

        if (updated == 0) {
            model.addAttribute("error", "Το αίτημα έχει ήδη ανατεθεί (ή αναλήφθηκε) από άλλον. Κάντε ανανέωση.");
            return reloadAdminRequests(model);
        }

        model.addAttribute("success", "Το αίτημα ανατέθηκε επιτυχώς.");
        return reloadAdminRequests(model);

    }

    private String reloadAdminRequests(Model model) {
        List<Request> requests = requestRepository.findAll();
        model.addAttribute("requests", requests);

        // Map: serviceUnitId -> employees της υπηρεσίας
        Set<Long> serviceUnitIds = requests.stream()
                .map(r -> r.getRequestType() == null || r.getRequestType().getServiceUnit() == null
                        ? null
                        : r.getRequestType().getServiceUnit().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, List<Person>> employeesByServiceUnit = new LinkedHashMap<>();
        for (Long suId : serviceUnitIds) {
            employeesByServiceUnit.put(
                    suId,
                    personRepository.findByTypeAndServiceUnit_IdOrderByEmailAddressAsc(PersonType.EMPLOYEE, suId)
            );
        }

        model.addAttribute("employeesByServiceUnit", employeesByServiceUnit);
        return "admin/requests-list :: content";
    }
}
