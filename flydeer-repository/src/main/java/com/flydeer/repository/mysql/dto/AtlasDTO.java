package com.flydeer.repository.mysql.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtlasDTO {

    private Long id;

    private String name;

    private String description;

    private Long authorId;

    private String authorName;

    private String status;

    private Boolean visible;

    private List<String> tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
