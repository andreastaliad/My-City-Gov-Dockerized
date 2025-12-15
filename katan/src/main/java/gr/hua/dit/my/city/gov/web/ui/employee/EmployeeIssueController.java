package gr.hua.dit.my.city.gov.web.ui.employee;

import gr.hua.dit.my.city.gov.core.model.Issue;
import gr.hua.dit.my.city.gov.core.model.IssueStatus;
import gr.hua.dit.my.city.gov.core.repository.IssueRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employee/issues")
public class EmployeeIssueController {

    private final IssueRepository issueRepository;

    public EmployeeIssueController(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
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

        return "redirect:/employee/issues";
    }
}
