package com.flydeer.contract.admin.request;

import com.flydeer.contract.common.request.ApiRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DisableUserRequest extends ApiRequest {

    /** Target user id to disable. */
    @NotNull(message = "目标用户ID不能为空")
    private Long operatorId;

    public DisableUserRequest(ApiRequest auth) {
        super(auth);
    }
}
