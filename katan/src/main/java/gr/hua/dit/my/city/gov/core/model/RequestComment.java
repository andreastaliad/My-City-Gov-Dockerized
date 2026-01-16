package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

//Οντότητα που επιτρέπει στον υπάλληλο να αφήσει σχόλια σε αιτήματα ή να δικαιολογήσει την απόρριψη ενός αιτήματος

@Entity
@Table(name = "request_comment")
public class RequestComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Σε ποιό αίτημα ανήκει το σχόλιο
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="request_id", nullable=false)
    private Request request;

    //Ποιός υπάλληλος το έγραψε
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="author_employee_id", nullable=false)
    private Person authorEmployee;

    @Column(name="comment_text", nullable=false, length=2000)
    private String text;

    //Πότε γράφτηκε
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    //Αυτόματο timestamp για την δημιουργία σχολίου
    @PrePersist
    void prePersist(){ if(createdAt==null) createdAt= LocalDateTime.now(); }

    //Getters-Setters
    public Long getId() {
        return id;
    }

    public Person getAuthorEmployee() {
        return authorEmployee;
    }

    public void setAuthorEmployee(Person authorEmployee) {
        this.authorEmployee = authorEmployee;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

}
