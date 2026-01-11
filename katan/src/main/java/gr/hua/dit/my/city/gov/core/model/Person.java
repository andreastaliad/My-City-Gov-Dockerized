package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
    name = "person",
    uniqueConstraints = {
            @UniqueConstraint(name = "uk_person_amka", columnNames = "amka"),
        @UniqueConstraint(name = "uk_person_afm", columnNames = "afm"),
        @UniqueConstraint(name = "uk_person_email_address", columnNames = "email_address"),
        @UniqueConstraint(name = "uk_person_phone_number", columnNames = "phone_number")
    },
    indexes = {
        @Index(name = "idx_person_type", columnList = "type"),
        @Index(name = "idx_person_last_name", columnList = "last_name")
    }
)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "afm", nullable = false, length = 9)
    private String afm;

    @Column(name = "amka", nullable = false, length = 11)
    private String amka;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", nullable = false, length = 18)
    private String phoneNumber; // E164

    @Column(name = "email_address", nullable = false, length = 100)
    private String emailAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PersonType type;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="service_unit_id")
    private ServiceUnit serviceUnit;

    public Person() {
    }

    public Person(Long id,
                  String afm,
                  String amka,
                  String firstName,
                  String lastName,
                  String phoneNumber,
                  String emailAddress,
                  PersonType type,
                  String passwordHash,
                  Instant createdAt) {
        this.id = id;
        this.afm = afm;
        this.amka = amka;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.type = type;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAfm() {
        return afm;
    }

    public String getAmka() {
        return amka;
    }

    public void setAfm (String afm) {
        this.afm = afm;
    }

    public void setAmka (String amka) {
        this.amka = amka;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public PersonType getType() {
        return type;
    }

    public void setType(PersonType type) {
        this.type = type;
    }


    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public ServiceUnit getServiceUnit() { return serviceUnit; }
    public void setServiceUnit(ServiceUnit serviceUnit) { this.serviceUnit = serviceUnit; }

    @Override
    public String toString() {
        return "Person{" +
            "first_name=" + firstName +
                ", last_name=" + lastName +
            ", afm='" + afm + '\'' +
                ", amka='" + amka + '\'' +
            '}';
    }
}
