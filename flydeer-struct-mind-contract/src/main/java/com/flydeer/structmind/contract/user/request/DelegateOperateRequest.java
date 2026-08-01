package com.flydeer.structmind.contract.user.request;

import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.user.enums.DelegateRelationEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelegateOperateRequest extends ApiRequest {

    @NotNull(message = "ID不能为空")
    private Long operateId;

    private DelegateRelationEnum relation;
}
