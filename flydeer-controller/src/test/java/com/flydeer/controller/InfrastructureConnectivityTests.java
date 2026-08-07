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
 * Verifies MySQL + PostgreSQL + Redis connectivity against locally running Compose services
 * ({@code docker compose up -d}). Skips when ports are unavailable.
 */
@SpringBootTest
class InfrastructureConnectivityTests {

    private static final String MYSQL_HOST = "127.0.0.1";
    private static final int MYSQL_PORT = 3306;
    private static final String POSTGRES_HOST = "127.0.0.1";
    private static final int POSTGRES_PORT = 5432;
    private static final String REDIS_HOST = "127.0.0.1";
    private static final int REDIS_PORT = 6379;

    @BeforeAll
    static void requireLocalInfra() {
        assumeTrue(isPortOpen(MYSQL_HOST, MYSQL_PORT), "MySQL not reachable on " + MYSQL_HOST + ":" + MYSQL_PORT);
        assumeTrue(
            isPortOpen(POSTGRES_HOST, POSTGRES_PORT),
            "PostgreSQL not reachable on " + POSTGRES_HOST + ":" + POSTGRES_PORT);
        assumeTrue(isPortOpen(REDIS_HOST, REDIS_PORT), "Redis not reachable on " + REDIS_HOST + ":" + REDIS_PORT);
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.mysql.url",
            () -> "jdbc:mysql://" + MYSQL_HOST + ":" + MYSQL_PORT
                + "/flydeer?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false");
        registry.add("spring.datasource.mysql.username", () -> "flydeer");
        registry.add("spring.datasource.mysql.password", () -> "flydeer");
        registry.add("spring.datasource.mysql.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add(
            "spring.datasource.postgres.url",
            () -> "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/flydeer_graph");
        registry.add("spring.datasource.postgres.username", () -> "flydeer");
        registry.add("spring.datasource.postgres.password", () -> "flydeer");
        registry.add("spring.datasource.postgres.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.data.redis.host", () -> REDIS_HOST);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS_PORT));
        registry.add("spring.docker.compose.enabled", () -> "false");
        registry.add("pagehelper.helper-dialect", () -> "mysql");
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("postgresDataSource")
    private DataSource postgresDataSource;

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
    void postgresIsReachable() throws Exception {
        try (Connection connection = postgresDataSource.getConnection();
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
