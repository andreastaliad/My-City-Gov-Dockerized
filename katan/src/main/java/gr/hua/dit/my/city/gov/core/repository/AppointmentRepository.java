package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalTime;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
	List<Appointment> findByPersonId(Long personId);
	List<Appointment> findByServiceUnitIdAndDate(Long serviceUnitId, LocalDate date);
	long countByServiceUnitId(Long serviceUnitId);
	List<Appointment> findByServiceUnitIdOrderByDateDescTimeDesc(Long serviceUnitId);

	//Confirm If Scheduled: επιβεβαίωση ενός ραντεβού μόνο αν είναι προγραμματισμένο
	@Modifying
	@Query("""
    update Appointment a
       set a.status = gr.hua.dit.my.city.gov.core.model.AppointmentStatus.CONFIRMED
     where a.id = :id
       and a.serviceUnitId = :serviceUnitId
       and a.status = gr.hua.dit.my.city.gov.core.model.AppointmentStatus.SCHEDULED
""")
	int confirmIfScheduled(@Param("id") Long id, @Param("serviceUnitId") Long serviceUnitId);

	//Reschedule If Not Cancelled: αναπρογραμματισμός ραντεβού αν δεν έχει ακυρωθεί
	@Modifying
	@Query("""
    update Appointment a
       set a.date = :newDate,
           a.time = :newTime,
           a.employeeNote = :note,
           a.status = gr.hua.dit.my.city.gov.core.model.AppointmentStatus.SCHEDULED
     where a.id = :id
       and a.serviceUnitId = :serviceUnitId
       and a.status <> gr.hua.dit.my.city.gov.core.model.AppointmentStatus.CANCELLED
""")
	int rescheduleIfNotCancelled(@Param("id") Long id,
								 @Param("serviceUnitId") Long serviceUnitId,
								 @Param("newDate") LocalDate newDate,
								 @Param("newTime") LocalTime newTime,
								 @Param("note") String note);

	//Cancel If Not Cancelled: ακύρωση ραντεβού αν δεν έχει ήδη ακυρωθεί
	@Modifying
	@Query("""
    update Appointment a
       set a.status = gr.hua.dit.my.city.gov.core.model.AppointmentStatus.CANCELLED,
           a.employeeNote = :reason
     where a.id = :id
       and a.serviceUnitId = :serviceUnitId
       and a.status <> gr.hua.dit.my.city.gov.core.model.AppointmentStatus.CANCELLED
""")
	int cancelIfNotCancelled(@Param("id") Long id,
							 @Param("serviceUnitId") Long serviceUnitId,
							 @Param("reason") String reason);

}
