package com.flydeer.contract.user.request;

import com.flydeer.common.constants.UserConstants;
import com.flydeer.contract.common.request.ApiRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRequest extends ApiRequest {

    @NotBlank(message = "用户名称不能为空")
    @Size(max = UserConstants.MAX_USER_NAME_LENGTH, message = "用户名称最多+" + UserConstants.MAX_USER_NAME_LENGTH + "个字符")
    private String name;

    public UpdateUserRequest(ApiRequest auth) {
        super(auth);
    }
}
