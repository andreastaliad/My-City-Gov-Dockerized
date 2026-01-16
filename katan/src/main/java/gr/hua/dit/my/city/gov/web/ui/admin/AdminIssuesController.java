package gr.hua.dit.my.city.gov.web.ui.admin;

import gr.hua.dit.my.city.gov.core.model.Issue;
import gr.hua.dit.my.city.gov.core.model.IssueStatus;
import gr.hua.dit.my.city.gov.core.repository.IssueRepository;
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

    public AdminIssuesController(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
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
        return "redirect:/admin/issues";
    }
}
