package com.flydeer.repository.postgres.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphEntity {

    private String graphId;

    private Long atlasId;

    private String name;

    private String parentGraphId;

    /** JSON text; written with CAST(... AS jsonb). */
    private String content;

    private Integer rev;

    private Integer nodeCount;

    private Integer deleted;

    private Long createdAt;

    private Long updatedAt;
}
