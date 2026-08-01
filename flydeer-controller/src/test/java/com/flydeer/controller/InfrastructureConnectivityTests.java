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
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:mysql://"
                        + MYSQL_HOST
                        + ":"
                        + MYSQL_PORT
                        + "/struct_mind?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false");
        registry.add("spring.datasource.username", () -> "struct_mind");
        registry.add("spring.datasource.password", () -> "struct_mind");
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.data.redis.host", () -> REDIS_HOST);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS_PORT));
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
