package gr.hua.dit.my.city.gov.core.service.model;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.web.rest.dto.EmployeeCreateRequest;
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

    public void registerEmployee(EmployeeCreateRequest request) {
        Person employee = new Person();
        employee.setEmailAddress(request.getEmailAddress());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setAfm(request.getAfm());
        employee.setAmka(request.getAmka());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setType(PersonType.EMPLOYEE);
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        personRepository.save(employee);
    }

}
