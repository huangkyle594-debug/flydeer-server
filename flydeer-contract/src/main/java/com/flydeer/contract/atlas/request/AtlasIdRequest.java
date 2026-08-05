package com.flydeer.contract.atlas.request;

import com.flydeer.contract.common.request.ApiRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AtlasIdRequest extends ApiRequest {

    @NotNull(message = "图集ID不能为空")
    private Long atlasId;

    public AtlasIdRequest(ApiRequest auth) {
        super(auth);
    }
}
