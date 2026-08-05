package com.flydeer.contract.atlas.request;

import com.flydeer.contract.common.request.ApiRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AtlasUpdateRequest extends ApiRequest {

    @NotNull(message = "图集ID不能为空")
    private Long atlasId;

    @Size(max = 64, message = "图集名称最多64个字符")
    private String name;

    @Size(max = 500, message = "图集简介最多500个字符")
    private String description;

    private List<@Size(max = 20, message = "标签最多20个字符") String> tags;

    public AtlasUpdateRequest(ApiRequest auth) {
        super(auth);
    }
}
