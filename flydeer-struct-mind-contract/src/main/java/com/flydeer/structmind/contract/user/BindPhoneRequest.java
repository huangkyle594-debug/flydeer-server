package com.flydeer.structmind.contract.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class BindPhoneRequest extends com.flydeer.structmind.contract.auth.BaseRequest {

    @NotBlank
    @Pattern(regexp = "^1\\d{10}$")
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
