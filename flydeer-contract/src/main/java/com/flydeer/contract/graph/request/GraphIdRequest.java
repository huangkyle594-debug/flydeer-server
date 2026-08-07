package com.flydeer.contract.graph.request;

import com.flydeer.contract.common.request.ApiRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GraphIdRequest extends ApiRequest {

    @NotBlank(message = "图ID不能为空")
    private String graphId;

    public GraphIdRequest(ApiRequest auth) {
        super(auth);
    }
}
