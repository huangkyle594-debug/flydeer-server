package com.flydeer.common;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * common 模块 Spring 入口。被上层模块通过 {@code @Import} 引入。
 */
@Configuration
@ComponentScan(basePackageClasses = CommonApplication.class)
public class CommonApplication {}
