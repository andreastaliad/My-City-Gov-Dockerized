package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.ServiceUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceUnitRepository extends JpaRepository<ServiceUnit, Long> {
    //Επιστρέφει όλες τις ενεργές υπηρεσίες
    List<ServiceUnit> findByActiveTrue();
    //Επιστρέφει όλες τις ενεργές υπηρεσίες ταξινομημένες αλφαβητικά
    //Χρησιμοποιείται σε dropdowns και admin views
    List<ServiceUnit> findByActiveTrueOrderByNameAsc();
}
