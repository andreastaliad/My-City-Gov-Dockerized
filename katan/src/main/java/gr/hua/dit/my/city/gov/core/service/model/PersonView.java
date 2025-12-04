package gr.hua.dit.my.city.gov.core.service.model;

import gr.hua.dit.my.city.gov.core.model.PersonType;

/**
 * PersonView (DTO) that includes only information to be exposed.
 */
public record PersonView(
    long id,
    String afm,
    String amka,
    String firstName,
    String lastName,
    String phoneNumber,
    String emailAddress,
    PersonType type
) {}
