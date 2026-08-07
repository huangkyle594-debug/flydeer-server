package com.flydeer.repository.postgres.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphDTO {

    private String graphId;

    private Long atlasId;

    private String name;

    private String parentGraphId;

    private JsonNode content;

    private Integer rev;

    private Integer nodeCount;

    private Integer deleted;

    private Long createdAt;

    private Long updatedAt;
}
