package gr.hua.dit.my.city.gov.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IssuesController {

    @GetMapping("/issues/form")
    public String showIssuesForm() {
        return "issues-form :: content";
    }
}

