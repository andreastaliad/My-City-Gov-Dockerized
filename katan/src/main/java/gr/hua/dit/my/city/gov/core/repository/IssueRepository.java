package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.Issue;
import gr.hua.dit.my.city.gov.core.model.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    //Επιστρέφει τα προβλήματα με την τωρινή τους κατάσταση
    List<Issue> findByStatus(IssueStatus status);
    //Επιστρέφει τα προβλήματα που ανέφερε ένας πολίτης
    List<Issue> findByPersonId(Long personId);
}