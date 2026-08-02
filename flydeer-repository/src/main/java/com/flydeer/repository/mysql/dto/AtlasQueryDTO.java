package com.flydeer.repository.mysql.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtlasQueryDTO {

    private Long viewerId;

    private Boolean editableOnly;

    private String keyword;

    private List<String> tags;
}
