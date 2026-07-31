package com.flydeer.structmind.service.user;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.flydeer.structmind.common.exception.auth.SmsSendException;
import com.flydeer.structmind.common.exception.auth.SmsVerifyException;
import com.flydeer.structmind.service.user.config.SmsConfig;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class SmsVerifyService {

    private final SmsConfig smsConfig;
    private final MockSmsProvider mockSmsProvider;
    private final AliyunSmsProvider aliyunSmsProvider;

    public SmsVerifyService(SmsConfig smsConfig) {
        this.smsConfig = smsConfig;
        this.mockSmsProvider = new MockSmsProvider();
        this.aliyunSmsProvider = new AliyunSmsProvider(smsConfig);
    }

    public void sendVerifyCode(String phone) throws SmsSendException {
        SmsProvider provider = smsConfig.isMockEnabled() ? mockSmsProvider : aliyunSmsProvider;
        provider.sendVerifyCode(phone);
    }

    public void checkVerifyCode(String phone, String code) throws SmsVerifyException {
        SmsProvider provider = smsConfig.isMockEnabled() ? mockSmsProvider : aliyunSmsProvider;
        provider.checkVerifyCode(phone, code);
    }

    private interface SmsProvider {

        SmsConfig getSmsConfig();

        void sendVerifyCode(String phone) throws SmsSendException;

        void checkVerifyCode(String phone, String code) throws SmsVerifyException;
    }

    private static class MockSmsProvider implements SmsProvider {

        @Override
        public SmsConfig getSmsConfig() {
            return null;
        }

        @Override
        public void sendVerifyCode(String phone) {
            log.info("Mock SMS verify code sent to {}", phone);
        }

        @Override
        public void checkVerifyCode(String phone, String code) {
            log.info("Mock SMS verify code accepted for {}", phone);
        }
    }

    private static class AliyunSmsProvider implements SmsProvider {

        @Getter
        private final SmsConfig smsConfig;

        public AliyunSmsProvider(SmsConfig smsConfig) {
            this.smsConfig = smsConfig;
        }

        @Override
        public void sendVerifyCode(String phone) throws SmsSendException {
            try (AsyncClient client = buildClient()) {
                SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
                    .phoneNumber(phone)
                    .signName(smsConfig.getSignName())
                    .templateCode(smsConfig.getTemplateCode())
                    .templateParam("{\"code\":\"##code##\",\"min\":\"5\"}")
                    .countryCode(smsConfig.getCountryCode())
                    .codeLength(6L)
                    .codeType(1L)
                    .build();
                SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request).get();
                Assert.isTrue(Boolean.TRUE.equals(response.getBody().getSuccess()), "send sms code fail");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new SmsSendException();
            } catch (Exception e) {
                throw new SmsSendException();
            }
        }

        @Override
        public void checkVerifyCode(String phone, String code) throws SmsVerifyException {
            try (AsyncClient client = buildClient()) {
                CheckSmsVerifyCodeRequest request = CheckSmsVerifyCodeRequest.builder()
                    .phoneNumber(phone)
                    .verifyCode(code)
                    .countryCode(smsConfig.getCountryCode())
                    .build();
                CheckSmsVerifyCodeResponse response = client.checkSmsVerifyCode(request).get();
                Assert.isTrue(Boolean.TRUE.equals(response.getBody().getSuccess()), "invalid verify sms code");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new SmsVerifyException();
            } catch (Exception e) {
                throw new SmsVerifyException();
            }
        }

        private AsyncClient buildClient() {
            StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(smsConfig.getAccessKeyId())
                .accessKeySecret(smsConfig.getAccessKeySecret())
                .build());
            return AsyncClient.builder()
                .region(smsConfig.getRegion())
                .credentialsProvider(provider)
                .overrideConfiguration(ClientOverrideConfiguration.create().setEndpointOverride(smsConfig.getEndpoint()))
                .build();
        }
    }
}
