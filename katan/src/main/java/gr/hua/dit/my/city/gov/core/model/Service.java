package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "service")

public class Service {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @NotBlank
    private String serviceName;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String serviceCode;

    @Column(length = 2000)
    private String description;

    private Integer slDays; //ουσιαστικα ειναι το deadline για να ολοκληρωθει ενα αιτημα(service level days) πχ αδεια σταθμευσης εχει sl=5 μερες

    private String department;

    public Service() {}

    public Service(Long serviceId,
            String serviceName,
            String serviceCode,
            String description,
            Integer slDays,
            String department) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceCode = serviceCode;
        this.description = description;
        this.slDays = slDays;
        this.department = department;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSlDays() {
        return slDays;
    }

    public void setSlDays(Integer slDays) {
        this.slDays = slDays;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
