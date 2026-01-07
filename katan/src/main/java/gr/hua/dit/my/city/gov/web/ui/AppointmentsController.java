package gr.hua.dit.my.city.gov.web.ui;

import gr.hua.dit.my.city.gov.core.model.Appointment;
import gr.hua.dit.my.city.gov.core.repository.AppointmentRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AppointmentsController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @GetMapping("/appointments/form")
    public String showAppointmentForm() {
        return "appointments-form :: content";
    }

    @PostMapping("/appointments")
    public String saveAppointment(Appointment appointment) {
        // associate with current user, if logged in
        currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .ifPresent(appointment::setPersonId);

        //για αποθήκευση στη βάση
        appointmentRepository.save(appointment);
        return "appointments-success :: content";
    }
}
