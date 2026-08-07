package com.flydeer.contract.graph.request;

import com.flydeer.contract.common.request.ApiRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GraphBatchSaveRequest extends ApiRequest {

    @NotNull(message = "图集ID不能为空")
    private Long atlasId;

    @NotEmpty(message = "graphs不能为空")
    @Size(max = 20, message = "单次最多保存20张图")
    @Valid
    private List<GraphBatchItemRequest> graphs;

    public GraphBatchSaveRequest(ApiRequest auth) {
        super(auth);
    }
}
