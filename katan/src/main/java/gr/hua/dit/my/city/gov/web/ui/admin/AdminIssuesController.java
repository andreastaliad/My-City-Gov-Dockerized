package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.model.Issue;
import gr.hua.dit.my.city.gov.core.model.IssueStatus;
import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.repository.IssueRepository;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.service.EmailSender;
import gr.hua.dit.my.city.gov.core.service.SmsSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.List;

//Controller υπεύθυνο για την διαχείριση των προβλημάτων

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminIssuesController {

    private final IssueRepository issueRepository;
    private final PersonRepository personRepository;
    private final SmsSender smsSender;
    private final EmailSender emailSender;

    public AdminIssuesController(IssueRepository issueRepository,
                                 PersonRepository personRepository,
                                 SmsSender smsSender,
                                 EmailSender emailSender) {
        this.issueRepository = issueRepository;
        this.personRepository = personRepository;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
    }

    @GetMapping("/issues")
    public String issues(
            @RequestParam(required = false) IssueStatus status,
            Model model
    ) {
        List<Issue> issues =
                (status == null)
                        ? issueRepository.findAll()
                        : issueRepository.findByStatus(status);

        model.addAttribute("issues", issues);
        model.addAttribute("statuses", IssueStatus.values());

        return "admin/issues-list :: content";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam IssueStatus status
    ) {
        Issue issue = issueRepository.findById(id).orElseThrow();
        issue.setStatus(status);
        issueRepository.save(issue);

        // Notifications to the citizen who reported the issue (same workflow as requests)
        Long personId = issue.getPersonId();
        if (personId != null) {
            Person citizen = personRepository.findById(personId).orElse(null);
            if (citizen != null) {
                String message = String.format(
                        "Your issue '%s' status is now: %s",
                        issue.getTitle(),
                        issue.getStatus().name()
                );

                if (citizen.getPhoneNumber() != null && !citizen.getPhoneNumber().isBlank()) {
                    smsSender.sendSms(citizen.getPhoneNumber(), message);
                }

                if (citizen.getEmailAddress() != null && !citizen.getEmailAddress().isBlank()) {
                    emailSender.sendSimpleEmail(
                            citizen.getEmailAddress(),
                            "My City Gov - Issue status updated",
                            message
                    );
                }
            }
        }
        return "redirect:/admin/issues";
    }
}
