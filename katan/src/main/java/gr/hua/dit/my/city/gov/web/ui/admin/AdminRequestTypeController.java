package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.model.RequestType;
import gr.hua.dit.my.city.gov.core.repository.RequestTypeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/request-types")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRequestTypeController {

    private final RequestTypeRepository requestTypeRepository;

    public AdminRequestTypeController(RequestTypeRepository requestTypeRepository) {
        this.requestTypeRepository = requestTypeRepository;
    }

    // Πίνακας τύπων
    @GetMapping
    public String list(Model model) {
        model.addAttribute("types", requestTypeRepository.findAll());
        return "admin/request-types-list :: content";
    }

    // Φόρμα νέου τύπου
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("requestType", new RequestType());
        return "admin/request-type-form :: content";
    }

    // Αποθήκευση
    @PostMapping
    public String create(RequestType type, Model model) {
        requestTypeRepository.save(type);
        model.addAttribute("types", requestTypeRepository.findAll());
        return "admin/request-types-list :: content";
    }

    // Ενεργοποίηση / Απενεργοποίηση
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, Model model) {
        RequestType type = requestTypeRepository.findById(id).orElseThrow();
        type.setActive(!type.isActive());
        requestTypeRepository.save(type);
        model.addAttribute("types", requestTypeRepository.findAll());
        return "admin/request-types-list :: content";
    }

}
