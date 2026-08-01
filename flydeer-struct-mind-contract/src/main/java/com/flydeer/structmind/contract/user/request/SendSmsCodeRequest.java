package com.flydeer.structmind.contract.user.request;

import com.flydeer.structmind.contract.base.request.ApiRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmsCodeRequest extends ApiRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式有误")
    private String phone;

    @NotBlank(message = "ip解析失败")
    private String ip;
}
