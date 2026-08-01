package com.flydeer.structmind.controller.controller;

import com.flydeer.structmind.contract.base.response.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/ping")
    public ApiResult<String> ping() {
        return ApiResult.ok("pong");
    }
}
