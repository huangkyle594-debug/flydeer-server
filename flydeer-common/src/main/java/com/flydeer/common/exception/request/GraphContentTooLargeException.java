package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;

public class GraphContentTooLargeException extends BadRequestException {

    public GraphContentTooLargeException(String message) {
        super(ErrorCodes.GRAPH_CONTENT_TOO_LARGE, message);
    }

    public GraphContentTooLargeException() {
        this("图内容超出体积限制");
    }
}
