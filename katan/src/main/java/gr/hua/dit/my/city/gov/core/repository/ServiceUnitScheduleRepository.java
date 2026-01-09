package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.ServiceUnitSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceUnitScheduleRepository extends JpaRepository<ServiceUnitSchedule, Long> {
    List<ServiceUnitSchedule> findByServiceUnitIdOrderByDayOfWeekAscStartTimeAsc(Long serviceUnitId);
}
