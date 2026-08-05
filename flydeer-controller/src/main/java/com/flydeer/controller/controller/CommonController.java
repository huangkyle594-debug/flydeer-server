package com.flydeer.controller.controller;

import com.flydeer.contract.common.CommonApi;
import com.flydeer.contract.common.response.ApiResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/common")
public class CommonController {

    private final CommonApi commonApi;

    @GetMapping("/notice")
    public ApiResult<String> notice() {
        return ApiResult.ok(commonApi.getNotice());
    }
}
