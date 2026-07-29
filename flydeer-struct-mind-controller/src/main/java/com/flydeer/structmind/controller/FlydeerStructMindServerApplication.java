package com.flydeer.structmind.controller;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.flydeer.structmind")
@MapperScan("com.flydeer.structmind.repository.mapper")
public class FlydeerStructMindServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlydeerStructMindServerApplication.class, args);
    }
}
