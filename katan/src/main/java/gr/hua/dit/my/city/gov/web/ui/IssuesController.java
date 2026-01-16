package gr.hua.dit.my.city.gov.web.ui;

import gr.hua.dit.my.city.gov.core.model.Issue;
import gr.hua.dit.my.city.gov.core.repository.IssueRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

//Controller υπεύθυνο για τα προβλήματα που αναφέρουν οι πολίτες

@Controller
public class IssuesController {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    //Φόρμα αναφοράς προβλήματος
    @GetMapping("/issues/form")
    public String showIssuesForm() {
        return "issues-form :: content";
    }

    //Αποθήκευση αναφοράς προβλήματος
    @PostMapping("/issues")
    public String saveIssue(Issue issue) {
        // associate with current user, if logged in
        currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .ifPresent(issue::setPersonId);

        issueRepository.save(issue);
        return "issues-success :: content";
    }
}

