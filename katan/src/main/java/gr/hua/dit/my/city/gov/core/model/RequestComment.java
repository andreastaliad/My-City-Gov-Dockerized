package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_comment")
public class RequestComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="request_id", nullable=false)
    private Request request;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="author_employee_id", nullable=false)
    private Person authorEmployee;

    @Column(name="comment_text", nullable=false, length=2000)
    private String text;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist(){ if(createdAt==null) createdAt= LocalDateTime.now(); }

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
