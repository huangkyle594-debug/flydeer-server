package com.flydeer.structmind.contract.user.request;

import com.flydeer.structmind.contract.base.request.ApiRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest extends ApiRequest {

    @NotBlank(message = "鉴权信息不能为空")
    private String refreshToken;
}
