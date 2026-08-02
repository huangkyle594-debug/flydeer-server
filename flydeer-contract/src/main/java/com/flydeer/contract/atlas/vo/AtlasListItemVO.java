package com.flydeer.contract.atlas.vo;

import java.util.List;

public record AtlasListItemVO(
    String id,
    String name,
    String description,
    String authorId,
    String authorName,
    String status,
    List<String> tags,
    List<String> graphIds,
    String rootGraphId,
    Long createdAt,
    Long updatedAt,
    boolean editable) {
}
