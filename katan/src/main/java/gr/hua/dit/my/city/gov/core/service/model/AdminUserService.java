package gr.hua.dit.my.city.gov.core.service.model;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {
    private final PersonRepository personRepository;

    public AdminUserService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Person> getAllEmployees() {
        return personRepository.findByType(PersonType.EMPLOYEE);
    }

    public List<Person> getAllCitizens() {
        return personRepository.findByType(PersonType.CITIZEN);
    }

    public Person getUser(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
