package com.flydeer.contract.graph.request;

import com.flydeer.contract.common.request.ApiRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GraphListRequest extends ApiRequest {

    @NotNull(message = "图集ID不能为空")
    private Long atlasId;

    private String keyword;

    public GraphListRequest(ApiRequest auth) {
        super(auth);
    }
}
