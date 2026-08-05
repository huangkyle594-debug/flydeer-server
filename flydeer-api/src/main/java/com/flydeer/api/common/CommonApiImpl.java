package com.flydeer.api.common;

import com.flydeer.contract.common.CommonApi;
import com.flydeer.service.common.config.CommonConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@AllArgsConstructor
public class CommonApiImpl implements CommonApi {

    private final CommonConfig commonConfig;

    @Override
    public String getNotice() {
        String notice = commonConfig.getNotice();
        return StringUtils.hasText(notice) ? notice : "";
    }
}
