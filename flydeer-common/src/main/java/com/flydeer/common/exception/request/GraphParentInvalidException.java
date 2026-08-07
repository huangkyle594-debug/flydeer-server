package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;

public class GraphParentInvalidException extends BadRequestException {

    public GraphParentInvalidException() {
        super(ErrorCodes.GRAPH_PARENT_INVALID, "目录位置非法");
    }
}
