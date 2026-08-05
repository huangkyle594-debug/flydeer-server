package com.flydeer.contract.user.request;

import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.user.enums.LoginChannelEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OauthLoginRequest extends ApiRequest {

    @NotNull(message = "未知登陆渠道")
    private LoginChannelEnum channel;

    public OauthLoginRequest(ApiRequest auth) {
        super(auth);
    }
}
