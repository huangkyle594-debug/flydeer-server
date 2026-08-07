package com.flydeer.contract.graph.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.flydeer.contract.common.request.ApiRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GraphSaveRequest extends ApiRequest {

    @NotBlank(message = "图ID不能为空")
    private String graphId;

    @NotNull(message = "图集ID不能为空")
    private Long atlasId;

    @NotBlank(message = "图名不能为空")
    @Size(max = 64, message = "图名最长64")
    private String name;

    private String parentGraphId;

    @NotNull(message = "rev不能为空")
    private Integer rev;

    @NotNull(message = "content不能为空")
    private JsonNode content;

    public GraphSaveRequest(ApiRequest auth) {
        super(auth);
    }
}
