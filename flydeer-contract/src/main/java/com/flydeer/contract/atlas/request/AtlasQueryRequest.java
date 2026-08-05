package com.flydeer.contract.atlas.request;

import com.flydeer.contract.atlas.enums.AtlasPermissionScope;
import com.flydeer.contract.common.request.ApiRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AtlasQueryRequest extends ApiRequest {

    private String keyword;

    /** 权限范围；默认 ALL */
    private AtlasPermissionScope scope;

    private List<String> tags;

    private int page = 1;

    private int pageSize = 10;

    public AtlasQueryRequest(ApiRequest auth) {
        super(auth);
    }

    public AtlasPermissionScope resolvedScope() {
        return scope == null ? AtlasPermissionScope.ALL : scope;
    }
}
