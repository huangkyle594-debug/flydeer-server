package com.flydeer.common.exception.business;

import com.flydeer.common.exception.ErrorCodes;

public class GraphNotFoundException extends BusinessException {

    public GraphNotFoundException() {
        super(ErrorCodes.GRAPH_NOT_FOUND, "图不存在或无权操作");
    }
}
