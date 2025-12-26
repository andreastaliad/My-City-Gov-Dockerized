package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.model.ServiceUnit;
import gr.hua.dit.my.city.gov.core.repository.ServiceUnitRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/service-units")
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceUnitController {

    private final ServiceUnitRepository serviceUnitRepository;

    public AdminServiceUnitController(ServiceUnitRepository serviceUnitRepository) {
        this.serviceUnitRepository = serviceUnitRepository;
    }

    // Πίνακας υπηρεσιών
    @GetMapping
    public String list(Model model) {
        model.addAttribute("serviceUnits", serviceUnitRepository.findAll());
        return "admin/service-units-list :: content";
    }

    // Φόρμα δημιουργίας
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("serviceUnit", new ServiceUnit());
        return "admin/service-unit-form :: content";
    }

    // Αποθήκευση
    @PostMapping
    public String save(@ModelAttribute ServiceUnit serviceUnit) {
        serviceUnitRepository.save(serviceUnit);
        return "redirect:/admin/service-units";
    }

    // Ενεργοποίηση / Απενεργοποίηση
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        ServiceUnit su = serviceUnitRepository.findById(id).orElseThrow();
        su.setActive(!su.isActive());
        serviceUnitRepository.save(su);
        return "redirect:/admin/service-units";
    }
}
