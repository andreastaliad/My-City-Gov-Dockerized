package gr.hua.dit.my.city.gov.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppointmentsController {

    @GetMapping("/appointments/form")
    public String showAppointmentForm() {
        return "appointments-form :: content";
    }
}
