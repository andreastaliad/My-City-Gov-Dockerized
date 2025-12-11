package gr.hua.dit.my.city.gov.web.ui;

import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import gr.hua.dit.my.city.gov.core.model.Request;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RequestsController {

    @Autowired
    private RequestRepository requestRepository;

    @GetMapping("/requests/form")
    public String showRequestsForm() {
        return "requests-form :: content";
    }

    @PostMapping("/requests")
    public String saveRequest(Request request) {
        requestRepository.save(request);
        return "requests-success :: content";
    }
}

