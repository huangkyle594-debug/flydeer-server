package com.flydeer.contract.atlas.request;

import com.flydeer.contract.base.request.ApiRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AtlasQueryRequest extends ApiRequest {

    private String keyword;

    private Boolean editable;

    private List<String> tags;

    private int page = 1;

    private int pageSize = 10;

    public AtlasQueryRequest(ApiRequest auth) {
        super(auth);
    }
}
