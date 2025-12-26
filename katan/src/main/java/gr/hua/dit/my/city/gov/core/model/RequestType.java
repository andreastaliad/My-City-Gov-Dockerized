package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class RequestType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private String service; // π.χ. Καθαριότητα, Τεχνική

    @Column(nullable = false)
    private boolean active = true;

    public RequestType() {}

    // Getters / Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
