package gr.hua.dit.my.city.gov.core.port;

import java.util.Optional;

import gr.hua.dit.my.city.gov.core.model.PersonType;

/**
 * Port to external service for managing lookups.
 */
public interface LookupPort {

    Optional<PersonType> lookup(final String afm);
}
