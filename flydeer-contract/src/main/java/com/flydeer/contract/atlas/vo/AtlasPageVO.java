package com.flydeer.contract.atlas.vo;

import java.util.List;

public record AtlasPageVO(
    List<AtlasListItemVO> items,
    boolean hasMore,
    long total) {
}
