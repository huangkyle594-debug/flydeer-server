package com.flydeer.service.atlas.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "app.atlas")
public class AtlasConfig {

    /**
     * Preset tags returned by the tags API (order preserved).
     */
    private List<String> tags = new ArrayList<>();
}
