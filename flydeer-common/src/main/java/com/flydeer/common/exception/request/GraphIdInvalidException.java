package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;

public class GraphIdInvalidException extends BadRequestException {

    public GraphIdInvalidException(String message) {
        super(ErrorCodes.GRAPH_ID_INVALID, message);
    }

    public GraphIdInvalidException() {
        this("图 ID 格式非法或已存在");
    }
}
