package com.flydeer.structmind.service;

import com.flydeer.structmind.repository.RepositoryApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * service 模块 Spring 入口。依赖 repository。
 */
@Configuration
@Import(RepositoryApplication.class)
@ComponentScan(basePackageClasses = ServiceApplication.class)
public class ServiceApplication {}
