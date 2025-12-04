package gr.hua.dit.my.city.gov.core.service;

import gr.hua.dit.my.city.gov.core.service.model.CreatePersonRequest;
import gr.hua.dit.my.city.gov.core.service.model.CreatePersonResult;

/**
 * Service for managing {@link gr.hua.dit.my.city.gov.core.model.Person}.
 */
public interface PersonService {

    CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify);

    default CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest) {
        return this.createPerson(createPersonRequest, true);
    }
}
