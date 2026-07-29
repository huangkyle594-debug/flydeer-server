package com.flydeer.structmind.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.flydeer.structmind")
@MapperScan("com.flydeer.structmind.repository.mapper")
public class FlydeerStructMindTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlydeerStructMindTaskApplication.class, args);
    }
}
