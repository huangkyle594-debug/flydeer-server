package com.flydeer.api;

import com.flydeer.service.ServiceApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * api 模块 Spring 入口。依赖 service。
 */
@Configuration
@Import(ServiceApplication.class)
@ComponentScan(basePackageClasses = ApiApplication.class)
public class ApiApplication {}
