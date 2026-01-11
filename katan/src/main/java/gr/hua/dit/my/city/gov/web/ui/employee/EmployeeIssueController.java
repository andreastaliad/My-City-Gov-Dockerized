package gr.hua.dit.my.city.gov.web.ui.employee;

import gr.hua.dit.my.city.gov.core.model.Issue;
import gr.hua.dit.my.city.gov.core.model.IssueStatus;
import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.repository.IssueRepository;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.service.EmailSender;
import gr.hua.dit.my.city.gov.core.service.SmsSender;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employee/issues")
public class EmployeeIssueController {

    private final IssueRepository issueRepository;
    private final PersonRepository personRepository;
    private final SmsSender smsSender;
    private final EmailSender emailSender;

    public EmployeeIssueController(IssueRepository issueRepository,
                                  PersonRepository personRepository,
                                  SmsSender smsSender,
                                  EmailSender emailSender) {
        this.issueRepository = issueRepository;
        this.personRepository = personRepository;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
    }

    @GetMapping
    public String listIssues(Model model) {
        List<Issue> issues = issueRepository.findAll();
        model.addAttribute("issues", issues);
        return "employee/issues-list";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam IssueStatus status) {

        Issue issue = issueRepository.findById(id).orElseThrow();
        issue.setStatus(status);
        issueRepository.save(issue);

        Long personId = issue.getPersonId();
        if (personId != null) {
            Person person = personRepository.findById(personId).orElse(null);
            if (person != null) {
                String message = String.format(
                        "Your issue '%s' status is now: %s",
                        issue.getTitle(),
                        issue.getStatus().name()
                );

                if (person.getPhoneNumber() != null && !person.getPhoneNumber().isBlank()) {
                    smsSender.sendSms(person.getPhoneNumber(), message);
                }

                if (person.getEmailAddress() != null && !person.getEmailAddress().isBlank()) {
                    emailSender.sendSimpleEmail(
                            person.getEmailAddress(),
                            "My City Gov - Issue status updated",
                            message
                    );
                }
            }
        }

        return "redirect:/employee/issues";
    }
}
