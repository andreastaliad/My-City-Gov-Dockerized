package gr.hua.dit.my.city.gov.web.rest.dto;

/**
 * Request DTO for OTP verification.
 */
public class OtpRequest {
    private String phone;
    private String otp;

    public OtpRequest() {
    }

    public OtpRequest(String phone, String otp) {
        this.phone = phone;
        this.otp = otp;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
