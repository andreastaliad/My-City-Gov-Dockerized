package gr.hua.dit.my.city.gov.web.ui;

import gr.hua.dit.my.city.gov.core.model.Issue;
import gr.hua.dit.my.city.gov.core.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class IssuesController {

    @Autowired
    private IssueRepository issueRepository;

    @GetMapping("/issues/form")
    public String showIssuesForm() {
        return "issues-form :: content";
    }

    @PostMapping("/issues")
    public String saveIssue(Issue issue) {
        issueRepository.save(issue);
        return "issues-success :: content";
    }
}

