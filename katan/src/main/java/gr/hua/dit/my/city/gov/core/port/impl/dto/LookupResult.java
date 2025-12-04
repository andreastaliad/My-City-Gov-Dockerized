package gr.hua.dit.my.city.gov.core.port.impl.dto;

import gr.hua.dit.my.city.gov.core.model.PersonType;

/**
 * LookupResult DTO.
 */

//TODO implement lookup for afm
public record LookupResult(
    String raw,
    boolean exists,
    String afm,
    PersonType type
) {}
