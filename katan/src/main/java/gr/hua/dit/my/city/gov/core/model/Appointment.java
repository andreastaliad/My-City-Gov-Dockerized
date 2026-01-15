package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private LocalTime time;
    private String service;

    @Column(name = "service_unit_id")
    private Long serviceUnitId;

    // ID of the person (citizen) who booked this appointment
    private Long personId;

    // Whether an SMS reminder has already been sent for this appointment
    private boolean reminderSent = false;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(length = 2000)
    private String employeeNote;

    //Getters-Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }
    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getServiceUnitId() {return serviceUnitId;}
    public void setServiceUnitId(Long serviceUnitId) {this.serviceUnitId = serviceUnitId;}

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getEmployeeNote() { return employeeNote; }
    public void setEmployeeNote(String employeeNote) { this.employeeNote = employeeNote; }

}
