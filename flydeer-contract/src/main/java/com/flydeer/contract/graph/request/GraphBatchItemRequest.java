package com.flydeer.contract.graph.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphBatchItemRequest {

    @NotBlank(message = "图ID不能为空")
    private String graphId;

    @NotBlank(message = "图名不能为空")
    @Size(max = 64, message = "图名最长64")
    private String name;

    private String parentGraphId;

    @NotNull(message = "rev不能为空")
    private Integer rev;

    @NotNull(message = "content不能为空")
    private JsonNode content;
}
