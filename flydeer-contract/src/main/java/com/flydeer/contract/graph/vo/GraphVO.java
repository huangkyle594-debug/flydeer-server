package com.flydeer.contract.graph.vo;

import com.fasterxml.jackson.databind.JsonNode;

public record GraphVO(
    String graphId,
    Long atlasId,
    String name,
    String parentGraphId,
    Integer nodeCount,
    Integer rev,
    Long createdAt,
    Long updatedAt,
    JsonNode content) {
}
