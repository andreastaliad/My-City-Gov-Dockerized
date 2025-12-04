package gr.hua.dit.my.city.gov.core.model;

/**
 * PersonView (DTO) that includes only information to be exposed.
 */
public record PersonView(
    long id,
    String afm,
    String amka,
    String firstName,
    String lastName,
    String mobilePhoneNumber,
    String emailAddress,
    PersonType type
) {}
