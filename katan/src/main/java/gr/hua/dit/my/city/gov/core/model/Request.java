package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;
import gr.hua.dit.my.city.gov.core.util.ProtocolNumberGenerator;
import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.CREATED;

    @PrePersist
    public void prePersist() {
        if (status == null) status = RequestStatus.CREATED;

        if (createdAt == null) createdAt = LocalDateTime.now();

        if (protocolNumber == null) protocolNumber = ProtocolNumberGenerator.newProtocol();

        // dueAt = createdAt + SLA (απόΑνάθεση αιτήματος σε συγκεκριμένο υπάλληλο (ή ανάληψη). τον τύπο)
        if (dueAt == null) {
            Integer sla = (requestType != null ? requestType.getSlaDays() : null);
            if (sla == null) sla = 10; // fallback (ή πέτα exception αν θες να είναι υποχρεωτικό)
            dueAt = createdAt.plusDays(sla);
        }
    }


    @Column(name = "protocol_number", nullable = false, unique = true, updatable = false, length = 32)
    private String protocolNumber;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    @Column
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    private Person assignedEmployee;

    private LocalDateTime assignedAt;

    // Απόφαση υπαλλήλου για ανατεθειμένα αιτήματα
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_decision", nullable = false)
    private EmployeeDecision employeeDecision = EmployeeDecision.PENDING;

    @Column(name = "employee_decision_reason", length = 2000)
    private String employeeDecisionReason;

    @Column(name = "employee_decided_at")
    private LocalDateTime employeeDecidedAt;

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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getProtocolNumber() { return protocolNumber; }
    public void setProtocolNumber(String protocolNumber) { this.protocolNumber = protocolNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Person getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(Person assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public EmployeeDecision getEmployeeDecision() {
        return employeeDecision;
    }

    public void setEmployeeDecision(EmployeeDecision employeeDecision) {
        this.employeeDecision = employeeDecision;
    }

    public String getEmployeeDecisionReason() {
        return employeeDecisionReason;
    }

    public void setEmployeeDecisionReason(String employeeDecisionReason) {
        this.employeeDecisionReason = employeeDecisionReason;
    }

    public LocalDateTime getEmployeeDecidedAt() {
        return employeeDecidedAt;
    }

    public void setEmployeeDecidedAt(LocalDateTime employeeDecidedAt) {
        this.employeeDecidedAt = employeeDecidedAt;
    }
}
