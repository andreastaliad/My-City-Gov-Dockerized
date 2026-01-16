package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;

//Παρουσιάζει τις υπηρεσίες-είναι υπεύθυνες για την διαχείριση τύπων αιτημάτων και την παράθεση ραντεβού σε πολίτες

@Entity
@Table(name = "service_unit")
public class ServiceUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    //Προαιρετικό
    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public ServiceUnit() {
    }

    public ServiceUnit(String name, String description) {
        this.name = name;
        this.description = description;
        this.active = true;
    }

    //Getters-Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}