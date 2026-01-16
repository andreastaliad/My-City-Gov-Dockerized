package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.RequestType;
import gr.hua.dit.my.city.gov.core.model.ServiceUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestTypeRepository extends JpaRepository<RequestType, Long> {
    //Επιστρέφει όλα τα ενεργά είδη αιτημάτων
    List<RequestType> findByActiveTrue();
    //Επιστρέφει τύπους αιτήματος ανά την υπηρεσία που τους χειρίζεται
    List<RequestType> findByServiceUnit(ServiceUnit serviceUnit);
}

