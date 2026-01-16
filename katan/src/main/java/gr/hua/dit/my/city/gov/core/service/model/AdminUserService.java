package gr.hua.dit.my.city.gov.core.service.model;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import org.springframework.stereotype.Service;
import java.util.List;

//Service για την διαχείριση των χρηστών του συστήματος
//Επιτρέπει read-only πρόσοψη για πολίτες και υπαλλήλους

@Service
public class AdminUserService {
    private final PersonRepository personRepository;

    public AdminUserService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    //Επιστρέφει τους εγγεγραμμένους υπαλλήλους
    public List<Person> getAllEmployees() {
        return personRepository.findByType(PersonType.EMPLOYEE);
    }

    //Επιστρέφει τους εγγεγραμμένους πολίτες
    public List<Person> getAllCitizens() {
        return personRepository.findByType(PersonType.CITIZEN);
    }

    //Παίρνει το user id
    public Person getUser(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
