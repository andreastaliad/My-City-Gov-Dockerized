package gr.hua.dit.my.city.gov.core.security;

import gr.hua.dit.my.city.gov.core.model.PersonType;

/**
 * @see CurrentUserProvider
 */
public record CurrentUser(long id, String emailAddress, PersonType type) {}
