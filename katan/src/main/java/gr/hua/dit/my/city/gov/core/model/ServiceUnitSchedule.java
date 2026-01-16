package gr.hua.dit.my.city.gov.core.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

//Είναι οντότητα που καθορίζει το εβδομαδιαίο πρόγραμμα μιας υπηρεσίας
//Χρησιμοποιείται για την δημιουργία ραντεβού-επιβεβαίωση
//Παρουσιάζει διαθεσιμότητα ανά μέρα μαζί με ώρες λειτουργίας και διάρκεια ραντεβού

@Entity
@Table(name = "service_unit_schedule")
public class ServiceUnitSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Σε ποιά υπηρεσία ανήκει το πρόγραμμα
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "service_unit_id", nullable = false)
    private ServiceUnit serviceUnit;

    //Για ποιές μέρες ισχύει το πρόγραμμα
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    //Διάρκεια ραντεβού
    @Column(nullable = false)
    private int slotMinutes = 15;

    //Δείχνει αν είναι ενεργή η εισαγωγή στο πρόγραμμα
    @Column(nullable = false)
    private boolean active = true;

    public ServiceUnitSchedule() {}

    //Getters-Setters
    public Long getId() { return id; }

    public ServiceUnit getServiceUnit() { return serviceUnit; }
    public void setServiceUnit(ServiceUnit serviceUnit) { this.serviceUnit = serviceUnit; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public int getSlotMinutes() { return slotMinutes; }
    public void setSlotMinutes(int slotMinutes) { this.slotMinutes = slotMinutes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}