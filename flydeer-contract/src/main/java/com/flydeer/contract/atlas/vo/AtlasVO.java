package com.flydeer.contract.atlas.vo;

import java.util.List;

public record AtlasVO(
    Long id,
    String name,
    String description,
    Long authorId,
    String authorName,
    String status,
    List<String> tags,
    Long createdAt,
    Long updatedAt,
    Boolean editable) {
}
