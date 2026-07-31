package com.flydeer.structmind.contract;

import com.flydeer.structmind.common.CommonApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * contract 模块 Spring 入口。依赖 common。
 */
@Configuration
@Import(CommonApplication.class)
@ComponentScan(basePackageClasses = ContractApplication.class)
public class ContractApplication {}
