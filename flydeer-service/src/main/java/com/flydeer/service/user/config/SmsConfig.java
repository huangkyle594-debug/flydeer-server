package com.flydeer.service.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云「短信认证」配置（号码认证服务 Dypnsapi，非短信服务 Dysmsapi）。
 *
 * <p>官方约定：发送/核验必须走 {@code SendSmsVerifyCode}/{@code CheckSmsVerifyCode}，
 * 签名与模板须使用号码认证控制台的<strong>赠送签名/赠送模板</strong>，不能与短信服务资源混用。
 * 验证码由阿里云生成（TemplateParam 使用 {@code ##code##}），业务侧不本地生成、不落库。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.sms")
public class SmsConfig {

    private String accessKeyId;
    private String accessKeySecret;
    /** 号码认证控制台赠送签名名称。 */
    private String signName;
    /** 号码认证控制台赠送模板 CODE，例如 100001。 */
    private String templateCode;
    private String region = "cn-shanghai";
    /** 固定为 dypnsapi.aliyuncs.com。 */
    private String endpoint = "dypnsapi.aliyuncs.com";
    private String countryCode = "86";
    private boolean mockEnabled;

    /** 验证码位数，4～8，默认 6。 */
    private int codeLength = 6;
    /** 验证码有效期（秒），默认 300。 */
    private int validTimeSeconds = 300;
    /** 阿里云侧发送间隔（秒），默认 60。 */
    private int sendIntervalSeconds = 60;
    /** 模板中有效期变量值（分钟），对应 TemplateParam.min。 */
    private int templateExpireMinutes = 5;
}
