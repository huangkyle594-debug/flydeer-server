package com.flydeer.contract.user.request;

import com.flydeer.contract.base.request.ApiRequest;
import com.flydeer.contract.user.enums.LoginChannelEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OauthCallbackRequest extends ApiRequest {

    @NotBlank(message = "未知登陆渠道")
    private LoginChannelEnum channel;

    @NotBlank(message = "登陆结果不能为空")
    private String code;

    @NotBlank(message = "签名不能为空")
    private String state;

    public OauthCallbackRequest(ApiRequest auth) {
        super(auth);
    }
}
