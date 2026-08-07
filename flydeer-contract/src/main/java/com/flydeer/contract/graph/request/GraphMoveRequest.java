package com.flydeer.contract.graph.request;

import com.flydeer.contract.common.request.ApiRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GraphMoveRequest extends ApiRequest {

    @NotBlank(message = "图ID不能为空")
    private String graphId;

    /** null 表示提升为根图；字段本身必填语义由接口约定（可显式传 null）。 */
    private String parentGraphId;

    public GraphMoveRequest(ApiRequest auth) {
        super(auth);
    }
}
