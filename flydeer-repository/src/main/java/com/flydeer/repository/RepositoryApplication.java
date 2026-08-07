package com.flydeer.repository;

import com.flydeer.contract.ContractApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * repository 模块 Spring 入口。依赖 contract；双数据源 MapperScan 见 config 包。
 */
@Configuration
@Import(ContractApplication.class)
@ComponentScan(basePackageClasses = RepositoryApplication.class)
public class RepositoryApplication {}
