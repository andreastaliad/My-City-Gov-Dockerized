package gr.hua.dit.my.city.gov.core.service.model;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeRegistrationService {
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeRegistrationService(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerEmployee(String email, String firstName, String lastName, String rawPassword) {
        Person employee = new Person();
        employee.setEmailAddress(email);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setType(PersonType.EMPLOYEE);
        employee.setPasswordHash(passwordEncoder.encode(rawPassword));
        employee.setAfm("000000000");
        employee.setAmka("00000000000");
        employee.setPhoneNumber("+300000000000");

        personRepository.save(employee);
    }
}
