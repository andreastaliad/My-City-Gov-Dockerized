package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;

@Entity
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    // ID of the person (citizen) who created this request
    private Long personId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "request_type_id", nullable = true)
    private RequestType requestType;

    // Citizen is represented by Person where Person.type == CITIZEN
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Person citizen;

    // Comma-separated MinIO object keys for uploaded attachments
    private String attachmentKey;
    public Request() {}

    //Getters-Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getAttachmentKey() {
        return attachmentKey;
    }

    public void setAttachmentKey(String attachmentKey) {
        this.attachmentKey = attachmentKey;
    }

    public Person getCitizen() { return citizen; }
    public void setCitizen(Person citizen) { this.citizen = citizen; }
}
