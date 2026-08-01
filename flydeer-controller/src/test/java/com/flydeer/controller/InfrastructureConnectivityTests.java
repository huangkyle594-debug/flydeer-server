package com.flydeer.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Verifies MySQL + Redis connectivity against locally running Compose services
 * ({@code docker compose up -d}). Skips when ports are unavailable.
 */
@SpringBootTest
class InfrastructureConnectivityTests {

    private static final String MYSQL_HOST = "127.0.0.1";
    private static final int MYSQL_PORT = 3306;
    private static final String REDIS_HOST = "127.0.0.1";
    private static final int REDIS_PORT = 6379;

    @BeforeAll
    static void requireLocalInfra() {
        assumeTrue(isPortOpen(MYSQL_HOST, MYSQL_PORT), "MySQL not reachable on " + MYSQL_HOST + ":" + MYSQL_PORT);
        assumeTrue(isPortOpen(REDIS_HOST, REDIS_PORT), "Redis not reachable on " + REDIS_HOST + ":" + REDIS_PORT);
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("MYSQL_HOST", () -> MYSQL_HOST);
        registry.add("MYSQL_PORT", () -> String.valueOf(MYSQL_PORT));
        registry.add("MYSQL_DATABASE", () -> "flydeer");
        registry.add("MYSQL_USER", () -> "flydeer");
        registry.add("MYSQL_PASSWORD", () -> "flydeer");
        registry.add("REDIS_HOST", () -> REDIS_HOST);
        registry.add("REDIS_PORT", () -> String.valueOf(REDIS_PORT));
        registry.add("SERVER_PORT", () -> "0");
        registry.add("AUTH_JWT_SECRET", () -> "test-jwt-secret-key-at-least-32-bytes!!");
        registry.add("AUTH_ACCESS_TTL", () -> "2h");
        registry.add("AUTH_REFRESH_TTL", () -> "90d");
        registry.add("AUTH_REFRESH_COOKIE", () -> "refresh_token");
        registry.add("AUTH_REFRESH_COOKIE_SECURE", () -> "false");
        registry.add("AUTH_REFRESH_COOKIE_PATH", () -> "/api/v1/auth");
        registry.add("AUTH_FRONTEND_REDIRECT", () -> "http://localhost:5173/oauth/callback");
        registry.add("ID_START", () -> "10000000");
        registry.add("ID_STEP_MIN", () -> "1");
        registry.add("ID_STEP_MAX", () -> "99");
        registry.add("SMS_MOCK_ENABLED", () -> "true");
        registry.add("SMS_ACCESS_KEY_ID", () -> "");
        registry.add("SMS_ACCESS_KEY_SECRET", () -> "");
        registry.add("SMS_SIGN_NAME", () -> "");
        registry.add("SMS_TEMPLATE_CODE", () -> "");
        registry.add("SMS_REGION", () -> "cn-shanghai");
        registry.add("SMS_ENDPOINT", () -> "dypnsapi.aliyuncs.com");
        registry.add("SMS_COUNTRY_CODE", () -> "86");
        registry.add("SMS_INTERVAL", () -> "60s");
        registry.add("SMS_DAILY_PHONE", () -> "20");
        registry.add("SMS_DAILY_IP", () -> "50");
        registry.add("LOGIN_INTERVAL", () -> "1s");
        registry.add("OAUTH_STATE_SECRET", () -> "test-oauth-state-hmac-secret!!");
        registry.add("OAUTH_STATE_TIMEOUT_MS", () -> "600000");
        registry.add("OAUTH_GITEE_CLIENT_ID", () -> "test-gitee-client-id");
        registry.add("OAUTH_GITEE_CLIENT_SECRET", () -> "test-gitee-client-secret");
        registry.add("OAUTH_GITEE_REDIRECT_URI", () -> "http://localhost:8080/api/v1/auth/gitee/callback");
        registry.add("OAUTH_GITEE_AUTHORIZE_URL", () -> "https://gitee.com/oauth/authorize");
        registry.add("OAUTH_GITEE_TOKEN_URL", () -> "https://gitee.com/oauth/token");
        registry.add("OAUTH_GITEE_USER_URL", () -> "https://gitee.com/api/v5/user");
        registry.add("OAUTH_GITEE_SCOPE", () -> "user_info");
        registry.add("OAUTH_GITHUB_CLIENT_ID", () -> "test-github-client-id");
        registry.add("OAUTH_GITHUB_CLIENT_SECRET", () -> "test-github-client-secret");
        registry.add("OAUTH_GITHUB_REDIRECT_URI", () -> "http://localhost:8080/api/v1/auth/github/callback");
        registry.add("OAUTH_GITHUB_AUTHORIZE_URL", () -> "https://github.com/login/oauth/authorize");
        registry.add("OAUTH_GITHUB_TOKEN_URL", () -> "https://github.com/login/oauth/access_token");
        registry.add("OAUTH_GITHUB_USER_URL", () -> "https://api.github.com/user");
        registry.add("OAUTH_GITHUB_SCOPE", () -> "read:user");
        registry.add("spring.docker.compose.enabled", () -> "false");
        registry.add("pagehelper.helper-dialect", () -> "mysql");
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void mysqlIsReachable() throws Exception {
        try (Connection connection = dataSource.getConnection();
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT 1")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(1);
        }
    }

    @Test
    void redisIsReachable() {
        try (var connection = redisConnectionFactory.getConnection()) {
            assertThat(connection.ping()).isEqualTo("PONG");
        }
    }

    private static boolean isPortOpen(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1_000);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
