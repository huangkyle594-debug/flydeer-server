package com.flydeer.contract.atlas.request;

import com.flydeer.contract.base.request.ApiRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AtlasImportRequest extends ApiRequest {

    private String format;

    private Integer version;

    private JsonNode atlas;

    private JsonNode graphs;

    public AtlasImportRequest(ApiRequest auth) {
        super(auth);
    }
}
