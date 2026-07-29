package com.flydeer.structmind.contract.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateNicknameRequest extends com.flydeer.structmind.contract.auth.BaseRequest {

    @NotBlank
    @Size(max = 64)
    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
