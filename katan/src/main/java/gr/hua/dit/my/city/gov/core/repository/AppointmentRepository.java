package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
	List<Appointment> findByPersonId(Long personId);
	List<Appointment> findByServiceUnitIdAndDate(Long serviceUnitId, LocalDate date);
	long countByServiceUnitId(Long serviceUnitId);
	List<Appointment> findByServiceUnitIdOrderByDateAscTimeAsc(Long serviceUnitId);
}
