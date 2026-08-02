package com.flydeer.repository.mysql.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AtlasEntity {
    private Long id;

    private String name;

    private String description;

    private Long authorId;

    private String authorName;

    private String status;

    private String tagsJson;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}