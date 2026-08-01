package com.flydeer.contract.user.request;

import com.flydeer.contract.base.request.ApiRequest;
import com.flydeer.contract.user.enums.DelegateRelationEnum;
import com.flydeer.contract.user.enums.DelegateStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QueryDelegateRequest extends ApiRequest {

    private List<DelegateStatusEnum> status;

    @NotBlank(message = "身份不能为空")
    private DelegateRelationEnum relation;

    public QueryDelegateRequest(ApiRequest auth) {
        super(auth);
    }

    public List<String> statusNullIfEmpty() {
        if (status == null || status.isEmpty()) {
            return null;
        }
        return status.stream().map(DelegateStatusEnum::name).toList();
    }
}
