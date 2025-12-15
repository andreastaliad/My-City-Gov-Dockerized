package gr.hua.dit.my.city.gov.web.ui.employee;

import gr.hua.dit.my.city.gov.core.model.Request;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/employee/requests")
public class EmployeeRequestController {

    private final RequestRepository requestRepository;

    public EmployeeRequestController(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @GetMapping
    public String listRequests(Model model) {
        List<Request> requests = requestRepository.findAll();
        model.addAttribute("requests", requests);
        return "employee/requests-list";
    }
}
