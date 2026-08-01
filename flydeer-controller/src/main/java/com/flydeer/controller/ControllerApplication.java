package com.flydeer.controller;

import com.flydeer.api.ApiApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * HTTP 可运行入口。通过 {@link ApiApplication} 传递整条模块依赖链。
 */
@SpringBootApplication
@Import(ApiApplication.class)
public class ControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControllerApplication.class, args);
    }
}
