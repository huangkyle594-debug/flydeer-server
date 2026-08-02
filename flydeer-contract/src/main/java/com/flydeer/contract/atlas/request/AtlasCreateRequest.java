package com.flydeer.contract.atlas.request;

import com.flydeer.contract.base.request.ApiRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AtlasCreateRequest extends ApiRequest {

    @NotBlank(message = "图集名称不能为空")
    @Size(max = 64, message = "图集名称最多64个字符")
    private String name;

    @Size(max = 500, message = "图集简介最多500个字符")
    private String description;

    private List<@Size(max = 20, message = "标签最多20个字符") String> tags;

    public AtlasCreateRequest(ApiRequest auth) {
        super(auth);
    }
}
