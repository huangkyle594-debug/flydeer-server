package com.flydeer.contract.user.request;

import com.flydeer.contract.base.request.ApiRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DisableUserRequest extends ApiRequest {

    @NotNull(message = "目标用户ID不能为空")
    private Long operatorId;

    public DisableUserRequest(ApiRequest auth) {
        super(auth);
    }
}
