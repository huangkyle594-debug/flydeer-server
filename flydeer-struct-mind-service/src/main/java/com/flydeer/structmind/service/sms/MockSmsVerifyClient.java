package com.flydeer.structmind.service.sms;

import com.flydeer.structmind.common.error.ErrorCodes;
import com.flydeer.structmind.common.exception.BusinessException;
import com.flydeer.structmind.service.config.AppAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Dev/mock SMS client used when {@code app.sms.mock-enabled=true}.
 */
@Service
@ConditionalOnProperty(prefix = "app.sms", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockSmsVerifyClient implements SmsVerifyClient {

    private static final Logger log = LoggerFactory.getLogger(MockSmsVerifyClient.class);

    private final AppAuthProperties properties;

    public MockSmsVerifyClient(AppAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public void sendVerifyCode(String phone) {
        if (!properties.getSms().isMockEnabled()) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "sms mock disabled");
        }
        log.info("Mock SMS verify code sent to {}", phone);
    }

    @Override
    public void checkVerifyCode(String phone, String code) {
        if (!properties.getSms().isMockEnabled()) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "sms mock disabled");
        }
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "invalid verify code");
        }
        log.info("Mock SMS verify code accepted for {}", phone);
    }
}
