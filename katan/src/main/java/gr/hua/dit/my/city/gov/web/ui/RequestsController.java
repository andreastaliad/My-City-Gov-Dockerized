package gr.hua.dit.my.city.gov.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RequestsController {

    @GetMapping("/requests/form")
    public String showRequestsForm() {
        return "requests-form :: content";
    }
}

