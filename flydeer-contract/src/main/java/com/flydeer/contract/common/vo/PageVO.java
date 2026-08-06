package com.flydeer.contract.common.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageVO<T> {

    private List<T> list;

    private Integer page;

    private Integer pageSize;

    private Long total;

    private Boolean hasMore;

    public PageVO(List<T> data, Boolean hasMore, Long total) {
        this.list = data;
        this.hasMore = hasMore;
        this.total = total;
    }

    public PageVO(List<T> data, Integer page, Integer pageSize, Long total) {
        this.list = data;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }
}
