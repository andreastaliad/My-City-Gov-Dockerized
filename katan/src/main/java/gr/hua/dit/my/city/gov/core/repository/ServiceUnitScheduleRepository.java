package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.ServiceUnitSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.DayOfWeek;
import java.util.List;

public interface ServiceUnitScheduleRepository extends JpaRepository<ServiceUnitSchedule, Long> {
    //Επιστρέφει ολόκληρο το εβδομαδιαίο πρόγραμμα μιας υπηρεσίας ταξινομημένο ανά μέρα και ώρα εκκίνησης
    List<ServiceUnitSchedule> findByServiceUnitIdOrderByDayOfWeekAscStartTimeAsc(Long serviceUnitId);
    //Επιστρέφει τις διαθέσιμες εισαγωγές στο πρόγραμμα για κάποια συγκεκριμένη μέρα
    //Χρησιμοποιείται όταν δημιουργείται ή επιβεβαιώνεται κάποιο ραντεβού
    List<ServiceUnitSchedule> findByServiceUnitIdAndDayOfWeekAndActiveTrue(Long serviceUnitId, DayOfWeek dayOfWeek);
}
