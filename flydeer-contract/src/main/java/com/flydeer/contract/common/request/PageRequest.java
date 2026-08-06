package com.flydeer.contract.common.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PageRequest<T> extends ApiRequest {

    @NotNull(message = "查询条件不能为空")
    private T query;

    private String orderBy = "id";

    private Boolean isAsc = true;

    @Max(value = 100, message = "最大查询100页")
    private Integer page = 1;

    @Max(value = 100, message = "分页查询最大值为100")
    private Integer pageSize = 10;

    public PageRequest(ApiRequest auth) {
        super(auth);
    }
}
