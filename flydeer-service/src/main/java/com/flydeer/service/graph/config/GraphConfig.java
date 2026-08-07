package com.flydeer.service.graph.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.graph")
public class GraphConfig {

    /** Max serialized size of a single graph content JSON, in bytes. */
    private int maxContentBytes = 2 * 1024 * 1024;

    /** Max total serialized size for batch-save, in bytes. */
    private int maxBatchBytes = 8 * 1024 * 1024;

    /** Max total serialized size for list-content response, in bytes. */
    private int maxListContentBytes = 20 * 1024 * 1024;

    private int maxBatchSize = 20;

    private int maxParentChainDepth = 100;
}
