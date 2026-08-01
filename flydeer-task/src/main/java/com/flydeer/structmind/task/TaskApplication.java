package com.flydeer.structmind.task;

import com.flydeer.structmind.service.ServiceApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 定时任务可运行入口。只依赖 service 链（不依赖 api）。
 */
@SpringBootApplication
@Import(ServiceApplication.class)
public class TaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskApplication.class, args);
    }
}
