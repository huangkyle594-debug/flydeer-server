package com.flydeer.contract.graph.vo;

public record GraphMetaVO(
    String graphId,
    Long atlasId,
    String name,
    String parentGraphId,
    Integer nodeCount,
    Integer rev,
    Long createdAt,
    Long updatedAt) {
}
