package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.ServiceUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceUnitRepository extends JpaRepository<ServiceUnit, Long> {
    List<ServiceUnit> findByActiveTrue();
}
