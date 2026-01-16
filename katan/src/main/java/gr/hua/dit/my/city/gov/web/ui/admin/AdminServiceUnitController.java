package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.model.ServiceUnit;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.repository.ServiceUnitRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

//Controller υπεύθυνος για την διαχείριση των υπηρεσιών την ανάθεση υπαλλήλων

@Controller
@RequestMapping("/admin/service-units")
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceUnitController {

    private final PersonRepository personRepository;
    private final ServiceUnitRepository serviceUnitRepository;

    public AdminServiceUnitController(ServiceUnitRepository serviceUnitRepository, PersonRepository personRepository) {
        this.serviceUnitRepository = serviceUnitRepository;
        this.personRepository = personRepository;
    }

    //Πίνακας υπηρεσιών
    @GetMapping
    public String list(Model model) {
        model.addAttribute("serviceUnits", serviceUnitRepository.findAll());
        model.addAttribute(
                "employees",
                personRepository.findByTypeOrderByEmailAddressAsc(PersonType.EMPLOYEE)
        );

        return "admin/service-units-list :: content";
    }

    //Φόρμα δημιουργίας
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("serviceUnit", new ServiceUnit());
        return "admin/service-unit-form :: content";
    }

    @GetMapping("/{id}/employees")
    public String employeesFragment(@PathVariable Long id, Model model) {

        ServiceUnit su = serviceUnitRepository.findById(id).orElseThrow();

        model.addAttribute("serviceUnit", su);

        //όλοι οι υπάλληλοι για να τους δείξει στη λίστα
        model.addAttribute("employees",
                personRepository.findByTypeOrderByEmailAddressAsc(PersonType.EMPLOYEE));

        return "admin/service-unit-employees :: content";
    }

    //ανάθεση υπαλλήλων
    @PostMapping("/{id}/employees")
    @Transactional
    public String updateEmployees(
            @PathVariable Long id,
            @RequestParam(required = false) List<Long> employeeIds,
            RedirectAttributes ra
    ) {
        ServiceUnit serviceUnit = serviceUnitRepository.findById(id).orElseThrow();

        //Αν δεν έρθει τίποτα, το θεωρούμε άδειο
        List<Long> ids = (employeeIds == null) ? List.of() : employeeIds;

        //Φέρε τους επιλεγμένους υπαλλήλους
        List<Person> selectedEmployees = personRepository.findAllById(ids);

        //VALIDATION: κανείς επιλεγμένος να μην ανήκει σε άλλη υπηρεσία(επιτρέπουμε μόνο: null ή αυτήν εδώ)
        for (Person emp : selectedEmployees) {
            if (emp.getType() != PersonType.EMPLOYEE) {
                ra.addFlashAttribute("error",
                        "Επιτράπηκε μόνο επιλογή υπαλλήλων.");
                return "redirect:/admin/service-units";
            }

            if (emp.getServiceUnit() != null && !emp.getServiceUnit().getId().equals(id)) {
                ra.addFlashAttribute("error",
                        "Ο υπάλληλος " + emp.getEmailAddress()
                                + " ανήκει ήδη στην υπηρεσία: "
                                + emp.getServiceUnit().getName());
                return "redirect:/admin/service-units";
            }
        }

        //Αφαίρεσε όλους τους υπαλλήλους από ΑΥΤΗ την υπηρεσία
        personRepository
                .findByTypeAndServiceUnit_IdOrderByEmailAddressAsc(PersonType.EMPLOYEE, id)
                .forEach(emp -> emp.setServiceUnit(null));

        //Ανάθεσε τους επιλεγμένους(που πλέον ξέρουμε ότι είναι valid)
        for (Person emp : selectedEmployees) {
            emp.setServiceUnit(serviceUnit);
        }

        //Δεν χρειάζεται save μέσα σε loops αν το persistence context είναι ενεργό(λόγω @Transactional)

        ra.addFlashAttribute("success", "Οι υπάλληλοι ενημερώθηκαν επιτυχώς.");
        return "redirect:/admin/service-units";
    }
    //Αποθήκευση
    @PostMapping
    public String save(@ModelAttribute ServiceUnit serviceUnit) {
        serviceUnitRepository.save(serviceUnit);
        return "redirect:/admin/service-units";
    }

    //Ενεργοποίηση/Απενεργοποίηση
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        ServiceUnit su = serviceUnitRepository.findById(id).orElseThrow();
        su.setActive(!su.isActive());
        serviceUnitRepository.save(su);
        return "redirect:/admin/service-units";
    }
}
