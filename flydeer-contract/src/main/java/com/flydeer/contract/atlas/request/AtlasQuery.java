package com.flydeer.contract.atlas.request;

import com.flydeer.contract.atlas.enums.AtlasPermissionScopeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AtlasQuery {

    private String keyword;

    private AtlasPermissionScopeEnum scope;

    private List<String> tags;

    /**
     * 后端赋值，前端无需关注
     */
    private Boolean visible;
}
