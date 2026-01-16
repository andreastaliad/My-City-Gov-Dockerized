package gr.hua.dit.my.city.gov.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//Controller για το homepage όλων των χρηστών

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home"; // home.html
    }
}

