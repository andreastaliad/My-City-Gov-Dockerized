package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.RequestComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {
    //Επιστρέφει όλα τα σχόλια για ένα αίτημα εμφανίζοντας τα πιο πρόσφατα πρώτα
    List<RequestComment> findByRequest_IdOrderByCreatedAtDesc(Long requestId);
}
