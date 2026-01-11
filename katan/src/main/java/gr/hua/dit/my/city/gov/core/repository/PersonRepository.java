package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Person} entity.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByAfm(final String afm);

    Optional<Person> findByEmailAddressIgnoreCase(final String emailAddress);

    List<Person> findByType(final PersonType type);

    boolean existsByEmailAddressIgnoreCase(final String emailAddress);

    boolean existsByPhoneNumber(final String phoneNumber);

    boolean existsByAfmIgnoreCase(final String afm);

    long countByType(PersonType type);

    List<Person> findByTypeOrderByEmailAddressAsc(PersonType type);

    List<Person> findByTypeAndServiceUnit_IdOrderByEmailAddressAsc(PersonType type, Long serviceUnitId);
}
