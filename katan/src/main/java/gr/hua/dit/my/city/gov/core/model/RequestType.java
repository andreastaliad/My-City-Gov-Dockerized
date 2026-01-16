package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;

//Κατηγοριοποιεί τα αιτήματα των πολιτών
//Κάθε είδος αιτήματος ανήκει σε υπηρεσία και έχει προθεσμία

@Entity
public class RequestType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    //Ποιά υπηρεσία είναι υπεύθυνη για τον τύπο
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_unit_id", nullable = false)
    private ServiceUnit serviceUnit;

    //Ελέγχει αν αυτός ο τύπος μπορεί να χρησιμοποιηθεί από τους πολίτες
    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer slaDays = 10;

    public RequestType() {}

    //Getters-Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ServiceUnit getServiceUnit() {
        return serviceUnit;
    }

    public void setServiceUnit(ServiceUnit serviceUnit) {
        this.serviceUnit = serviceUnit;
    }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Integer getSlaDays() { return slaDays; }
    public void setSlaDays(Integer slaDays) { this.slaDays = slaDays; }
}
