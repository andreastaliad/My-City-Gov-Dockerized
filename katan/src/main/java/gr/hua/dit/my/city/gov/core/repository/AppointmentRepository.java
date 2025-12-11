package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
