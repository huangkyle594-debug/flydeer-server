package com.flydeer.structmind.contract.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SmsSendRequest {

    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "invalid phone")
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
