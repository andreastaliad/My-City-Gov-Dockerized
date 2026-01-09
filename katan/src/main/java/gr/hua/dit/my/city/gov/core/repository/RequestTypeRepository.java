package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.RequestType;
import gr.hua.dit.my.city.gov.core.model.ServiceUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestTypeRepository extends JpaRepository<RequestType, Long> {
    List<RequestType> findByActiveTrue();
    List<RequestType> findByServiceUnit(ServiceUnit serviceUnit);
}

