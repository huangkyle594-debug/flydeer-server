package com.flydeer.structmind.service.sms;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.flydeer.structmind.common.error.ErrorCodes;
import com.flydeer.structmind.common.exception.BusinessException;
import com.flydeer.structmind.service.config.AppAuthProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import java.util.concurrent.ExecutionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(prefix = "app.sms", name = "mock-enabled", havingValue = "false")
public class AliyunSmsVerifyClient implements SmsVerifyClient {

    private final AppAuthProperties properties;

    public AliyunSmsVerifyClient(AppAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public void sendVerifyCode(String phone) {
        AppAuthProperties.Sms sms = properties.getSms();
        try (AsyncClient client = buildClient()) {
            SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
                    .phoneNumber(phone)
                    .signName(sms.getSignName())
                    .templateCode(sms.getTemplateCode())
                    .templateParam("{\"code\":\"##code##\",\"min\":\"5\"}")
                    .countryCode(sms.getCountryCode())
                    .codeLength(6L)
                    .codeType(1L)
                    .build();
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request).get();
            if (response.getBody() == null || !Boolean.TRUE.equals(response.getBody().getSuccess())) {
                String msg = response.getBody() != null ? response.getBody().getMessage() : "send failed";
                throw new BusinessException(ErrorCodes.BAD_REQUEST, msg);
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "sms send interrupted");
        } catch (ExecutionException ex) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "sms send failed: " + ex.getMessage());
        }
    }

    @Override
    public void checkVerifyCode(String phone, String code) {
        AppAuthProperties.Sms sms = properties.getSms();
        try (AsyncClient client = buildClient()) {
            CheckSmsVerifyCodeRequest request = CheckSmsVerifyCodeRequest.builder()
                    .phoneNumber(phone)
                    .verifyCode(code)
                    .countryCode(sms.getCountryCode())
                    .build();
            CheckSmsVerifyCodeResponse response = client.checkSmsVerifyCode(request).get();
            if (response.getBody() == null || !Boolean.TRUE.equals(response.getBody().getSuccess())) {
                throw new BusinessException(ErrorCodes.BAD_REQUEST, "invalid verify code");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "sms check interrupted");
        } catch (ExecutionException ex) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "sms check failed: " + ex.getMessage());
        }
    }

    private AsyncClient buildClient() {
        AppAuthProperties.Sms sms = properties.getSms();
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(sms.getAccessKeyId())
                .accessKeySecret(sms.getAccessKeySecret())
                .build());
        return AsyncClient.builder()
                .region(sms.getRegion())
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create().setEndpointOverride(sms.getEndpoint()))
                .build();
    }
}
