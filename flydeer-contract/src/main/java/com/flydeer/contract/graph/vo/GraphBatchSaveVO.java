package com.flydeer.contract.graph.vo;

import java.util.List;

public record GraphBatchSaveVO(int okCount, List<GraphSaveResultVO> results) {
}
