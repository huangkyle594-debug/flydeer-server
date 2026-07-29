package com.flydeer.structmind.contract.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SmsLoginRequest {

    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "invalid phone")
    private String phone;

    @NotBlank
    private String code;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
