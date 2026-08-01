package com.flydeer.structmind.contract.user.request;

import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.user.enums.LoginChannelEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OauthLoginRequest extends ApiRequest {

    @NotNull(message = "未知登陆渠道")
    private LoginChannelEnum channel;
}
