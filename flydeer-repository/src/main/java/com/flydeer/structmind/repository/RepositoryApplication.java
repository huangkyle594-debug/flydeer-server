package com.flydeer.structmind.repository;

import com.flydeer.structmind.contract.ContractApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * repository 模块 Spring 入口。依赖 contract；统一声明 MapperScan。
 */
@Configuration
@Import(ContractApplication.class)
@ComponentScan(basePackageClasses = RepositoryApplication.class)
@MapperScan("com.flydeer.structmind.repository.mysql.mapper")
public class RepositoryApplication {}
