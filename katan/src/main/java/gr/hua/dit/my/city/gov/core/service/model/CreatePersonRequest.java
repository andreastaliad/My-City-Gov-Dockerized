package gr.hua.dit.my.city.gov.core.service.model;

import gr.hua.dit.my.city.gov.core.model.PersonType;

/**
 * DTO for requesting the creation (registration) of a Person.
 */
public record CreatePersonRequest(
    PersonType type,
    String afm,
    String amka,
    String firstName,
    String lastName,
    String emailAddress,
    String phoneNumber,
    String passwordHash,
    String rawPassword
) {}
