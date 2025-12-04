package gr.hua.dit.my.city.gov.core.port.impl.dto;

/**
 * PhoneNumberValidationResult DTO.
 */

//TODO code verification 2fa
public record PhoneNumberValidationResult(
        String raw,
        boolean valid,
        String type,
        String e164
) {

    public boolean isValid() {
        return this.valid;
    }

    public boolean isValidMobile() {
        if (!this.valid) return false;
        if (this.type == null) return false;
        return "mobile".equals(this.type);
    }
}
