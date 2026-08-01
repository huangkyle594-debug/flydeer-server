package com.flydeer.structmind.contract.user.request;

import com.flydeer.structmind.common.constants.UserConstants;
import com.flydeer.structmind.contract.base.request.ApiRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest extends ApiRequest {

    @NotBlank(message = "用户名称不能为空")
    @Size(max = UserConstants.MAX_USER_NAME_LENGTH, message = "用户名称最多+" + UserConstants.MAX_USER_NAME_LENGTH + "个字符")
    private String name;
}
