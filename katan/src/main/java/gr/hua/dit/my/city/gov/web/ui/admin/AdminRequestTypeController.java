package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.model.RequestType;
import gr.hua.dit.my.city.gov.core.repository.RequestTypeRepository;
import gr.hua.dit.my.city.gov.core.repository.ServiceUnitRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//Controller υπεύθυνο για την διαχείριση ειδών αιτήματος

@Controller
@RequestMapping("/admin/request-types")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRequestTypeController {

    private final RequestTypeRepository requestTypeRepository;
    private final ServiceUnitRepository serviceUnitRepository;

    public AdminRequestTypeController(RequestTypeRepository requestTypeRepository, ServiceUnitRepository serviceUnitRepository) {
        this.requestTypeRepository = requestTypeRepository;
        this.serviceUnitRepository = serviceUnitRepository;
    }

    //Πίνακας τύπων
    @GetMapping
    public String list(Model model) {
        model.addAttribute("types", requestTypeRepository.findAll());
        return "admin/request-types-list :: content";
    }

    //Φόρμα νέου τύπου
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("requestType", new RequestType());
        model.addAttribute("serviceUnits", serviceUnitRepository.findByActiveTrue());
        return "admin/request-type-form :: content";
    }

    //Αποθήκευση
    @PostMapping
    public String create(RequestType type,
                         @RequestParam("serviceUnitId") Long serviceUnitId,
                         Model model) {

        var serviceUnit = serviceUnitRepository.findById(serviceUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Service Unit not found"));

        type.setServiceUnit(serviceUnit);

        requestTypeRepository.save(type);

        model.addAttribute("types", requestTypeRepository.findAll());
        return "admin/request-types-list :: content";
    }

    //Ενεργοποίηση/Απενεργοποίηση
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, Model model) {
        RequestType type = requestTypeRepository.findById(id).orElseThrow();
        type.setActive(!type.isActive());
        requestTypeRepository.save(type);
        model.addAttribute("types", requestTypeRepository.findAll());
        return "admin/request-types-list :: content";
    }

}
