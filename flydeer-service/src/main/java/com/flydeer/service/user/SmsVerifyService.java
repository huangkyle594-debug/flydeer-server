package com.flydeer.service.user;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponseBody;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.flydeer.common.exception.auth.SmsSendException;
import com.flydeer.common.exception.auth.SmsVerifyException;
import com.flydeer.service.user.config.SmsConfig;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 阿里云短信认证（Dypnsapi）：云侧生成+校验验证码，本地不生成、不缓存验证码。
 */
@Slf4j
@Service
public class SmsVerifyService {

    private static final String OK = "OK";
    private static final String VERIFY_PASS = "PASS";

    private final SmsConfig smsConfig;
    private final MockSmsProvider mockSmsProvider;
    private final AliyunSmsAuthProvider aliyunSmsAuthProvider;

    public SmsVerifyService(SmsConfig smsConfig) {
        this.smsConfig = smsConfig;
        this.mockSmsProvider = new MockSmsProvider();
        this.aliyunSmsAuthProvider = new AliyunSmsAuthProvider(smsConfig);
        log.info(
            "SMS auth config: mockEnabled={}, accessKeyIdSet={}, signName={}, templateCode={}, endpoint={}",
            smsConfig.isMockEnabled(),
            StringUtils.hasText(smsConfig.getAccessKeyId()),
            smsConfig.getSignName(),
            smsConfig.getTemplateCode(),
            smsConfig.getEndpoint());
    }

    public void sendVerifyCode(String phone) throws SmsSendException {
        SmsProvider provider = smsConfig.isMockEnabled() ? mockSmsProvider : aliyunSmsAuthProvider;
        provider.sendVerifyCode(phone);
    }

    public void checkVerifyCode(String phone, String code) throws SmsVerifyException {
        SmsProvider provider = smsConfig.isMockEnabled() ? mockSmsProvider : aliyunSmsAuthProvider;
        provider.checkVerifyCode(phone, code);
    }

    private interface SmsProvider {

        void sendVerifyCode(String phone) throws SmsSendException;

        void checkVerifyCode(String phone, String code) throws SmsVerifyException;
    }

    private static class MockSmsProvider implements SmsProvider {

        @Override
        public void sendVerifyCode(String phone) {
            log.info("Mock SMS verify code sent to {}", phone);
        }

        @Override
        public void checkVerifyCode(String phone, String code) {
            log.info("Mock SMS verify code accepted for {}", phone);
        }
    }

    /**
     * 号码认证「短信认证」：SendSmsVerifyCode + CheckSmsVerifyCode。
     * TemplateParam 使用 ##code##，由阿里云生成验证码；核验以 Model.VerifyResult=PASS 为准。
     */
    @AllArgsConstructor
    private static class AliyunSmsAuthProvider implements SmsProvider {

        private final SmsConfig smsConfig;

        @Override
        public void sendVerifyCode(String phone) throws SmsSendException {
            try (AsyncClient client = buildClient()) {
                String templateParam = String.format(
                    "{\"code\":\"##code##\",\"min\":\"%d\"}", smsConfig.getTemplateExpireMinutes());
                SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
                    .phoneNumber(phone)
                    .signName(smsConfig.getSignName())
                    .templateCode(smsConfig.getTemplateCode())
                    .templateParam(templateParam)
                    .countryCode(smsConfig.getCountryCode())
                    .codeLength((long) smsConfig.getCodeLength())
                    .codeType(1L)
                    .validTime((long) smsConfig.getValidTimeSeconds())
                    .interval((long) smsConfig.getSendIntervalSeconds())
                    .build();
                SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request).get();
                SendSmsVerifyCodeResponseBody body = response.getBody();
                if (body == null || !OK.equalsIgnoreCase(body.getCode())) {
                    log.warn(
                        "Aliyun SMS auth send rejected: phone={}, code={}, message={}, success={}",
                        phone,
                        body == null ? null : body.getCode(),
                        body == null ? null : body.getMessage(),
                        body == null ? null : body.getSuccess());
                    throw new SmsSendException();
                }
            } catch (SmsSendException e) {
                throw e;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new SmsSendException();
            } catch (Exception e) {
                log.warn(
                    "Aliyun SMS auth send failed: phone={}, signName={}, templateCode={}, err={}",
                    phone,
                    smsConfig.getSignName(),
                    smsConfig.getTemplateCode(),
                    e.toString());
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
                CheckSmsVerifyCodeResponseBody body = response.getBody();
                // 接口 Success/Code=OK 只表示调用成功；是否通过以 VerifyResult=PASS 为准
                String verifyResult = body == null || body.getModel() == null
                    ? null
                    : body.getModel().getVerifyResult();
                if (body == null
                    || !OK.equalsIgnoreCase(body.getCode())
                    || !VERIFY_PASS.equalsIgnoreCase(verifyResult)) {
                    log.warn(
                        "Aliyun SMS auth verify rejected: phone={}, apiCode={}, message={}, verifyResult={}",
                        phone,
                        body == null ? null : body.getCode(),
                        body == null ? null : body.getMessage(),
                        verifyResult);
                    throw new SmsVerifyException();
                }
            } catch (SmsVerifyException e) {
                throw e;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new SmsVerifyException();
            } catch (Exception e) {
                log.warn("Aliyun SMS auth verify failed: phone={}, err={}", phone, e.toString());
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
                .overrideConfiguration(
                    ClientOverrideConfiguration.create().setEndpointOverride(smsConfig.getEndpoint()))
                .build();
        }
    }
}
