package com.flydeer.contract.user.request;

import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.user.enums.DelegateRelationEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DelegateOperateRequest extends ApiRequest {

    @NotNull(message = "ID不能为空")
    private Long operateId;

    private DelegateRelationEnum relation;

    public DelegateOperateRequest(ApiRequest auth) {
        super(auth);
    }
}
