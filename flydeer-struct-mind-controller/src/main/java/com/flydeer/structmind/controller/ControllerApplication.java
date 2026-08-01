package com.flydeer.structmind.controller;

import com.flydeer.structmind.api.ApiApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * HTTP 可运行入口。通过 {@link ApiApplication} 传递整条模块依赖链。
 */
@SpringBootApplication
@Import(ApiApplication.class)
@EnableConfigurationProperties(AuthProperties.class)
public class ControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControllerApplication.class, args);
    }
}
