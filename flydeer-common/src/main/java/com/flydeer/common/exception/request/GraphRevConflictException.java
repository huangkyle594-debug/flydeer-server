package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;
import lombok.Getter;

@Getter
public class GraphRevConflictException extends Exception {

    private final int code;
    private final int rev;

    public GraphRevConflictException(int rev) {
        super("图已被修改，请刷新后重试");
        this.code = ErrorCodes.GRAPH_REV_CONFLICT;
        this.rev = rev;
    }
}
