package gr.hua.dit.my.city.gov.web.ui.employee;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.Request;
import gr.hua.dit.my.city.gov.core.model.RequestStatus;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import gr.hua.dit.my.city.gov.core.service.EmailSender;
import gr.hua.dit.my.city.gov.core.service.SmsSender;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employee/requests")
public class EmployeeRequestController {

    private final RequestRepository requestRepository;
    private final SmsSender smsSender;
    private final EmailSender emailSender;

    public EmployeeRequestController(RequestRepository requestRepository,
                                     SmsSender smsSender,
                                     EmailSender emailSender) {
        this.requestRepository = requestRepository;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
    }

    @GetMapping
    public String listRequests(Model model) {
        List<Request> requests = requestRepository.findAll();
        model.addAttribute("requests", requests);
        return "employee/requests-list";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam RequestStatus status) {

        Request request = requestRepository.findById(id).orElseThrow();
        request.setStatus(status);

        if (status == RequestStatus.COMPLETED || status == RequestStatus.CLOSED) {
            if (request.getCompletedAt() == null) {
                request.setCompletedAt(LocalDateTime.now());
            }
        }

        request = requestRepository.save(request);

        Person citizen = request.getCitizen();
        if (citizen != null) {
            String message = String.format(
                    "Your request '%s' (protocol %s) status is now: %s",
                    request.getTitle(),
                    request.getProtocolNumber(),
                    request.getStatus().name()
            );

            if (citizen.getPhoneNumber() != null && !citizen.getPhoneNumber().isBlank()) {
                smsSender.sendSms(citizen.getPhoneNumber(), message);
            }

            if (citizen.getEmailAddress() != null && !citizen.getEmailAddress().isBlank()) {
                emailSender.sendSimpleEmail(
                        citizen.getEmailAddress(),
                        "My City Gov - Request status updated",
                        message
                );
            }
        }

        return "redirect:/employee/requests";
    }
}
