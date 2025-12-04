package gr.hua.dit.my.city.gov.core.service.mapper;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.service.model.PersonView;

import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link Person} to {@link PersonView}.
 */
@Component
public class PersonMapper {

    public PersonView convertPersonToPersonView(final Person person) {
        if (person == null) {
            return null;
        }
        return new PersonView(
            person.getId(),
            person.getAfm(),
            person.getAmka(),
            person.getFirstName(),
            person.getLastName(),
            person.getPhoneNumber(),
            person.getEmailAddress(),
            person.getType()
        );
    }
}
