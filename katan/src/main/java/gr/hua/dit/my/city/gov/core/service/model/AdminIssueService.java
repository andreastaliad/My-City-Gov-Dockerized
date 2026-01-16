package gr.hua.dit.my.city.gov.core.service.model;

import gr.hua.dit.my.city.gov.core.model.Issue;
import gr.hua.dit.my.city.gov.core.model.IssueStatus;
import gr.hua.dit.my.city.gov.core.repository.IssueRepository;
import org.springframework.stereotype.Service;
import java.util.List;

//Service για την διαχείριση των αναφερθέντων προβλημάτων
//Επιτρέπει την προβολή και ενημέρωση κατάστασης των προβλημάτων

@Service
public class AdminIssueService {

    private final IssueRepository issueRepository;

    public AdminIssueService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    //Επιστρέφει όλα τα προβλήματα
    public List<Issue> getAll() {
        return issueRepository.findAll();
    }

    //Επιστρέφει τα προβλήματα ανά κατάσταση
    public List<Issue> getByStatus(IssueStatus status) {
        return issueRepository.findByStatus(status);
    }

    //Ενημέρωση κατάστασης προβλήματος
    public void updateStatus(Long issueId, IssueStatus status) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow();
        issue.setStatus(status);
        issueRepository.save(issue);
    }
}
