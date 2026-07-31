# FlyDeer Struct Mind Server

Spring Boot 多模块后端：JDK 21 · Spring Boot 3.5 · Maven · MyBatis · PageHelper · MySQL · Redis。

## 模块依赖

```
common  ←  contract  ←  repository  ←  service  ←  api  ←  controller  (HTTP Boot)
                                    ↘
                                      task  (定时任务 Boot，不依赖 api)
```

| 模块 | Artifact | 职责 |
|---|---|---|
| common | `flydeer-struct-mind-common` | 工具、异常、统一响应、Jackson |
| contract | `flydeer-struct-mind-contract` | DTO / 枚举 / 跨模块接口 |
| repository | `flydeer-struct-mind-repository` | MyBatis Mapper / Entity / PageHelper |
| service | `flydeer-struct-mind-service` | 可复用业务逻辑、事务、Redis |
| api | `flydeer-struct-mind-api` | 面向业务的用例编排 |
| task | `flydeer-struct-mind-task` | 独立定时任务应用（端口 8081） |
| controller | `flydeer-struct-mind-controller` | HTTP API + 可运行 Boot 应用（端口 8080） |

规则：上层依赖下层；禁止反向依赖（如 repository 不得依赖 service）。controller 依赖 api；task 只依赖 service。

## 快速开始

```bash
./bash/run.sh                 # 一键：Compose + controller (8080)
./bash/run.sh --with-task     # 同时启动 task (8081)
./bash/run.sh stop            # 停止应用
./bash/run.sh down            # 停止应用并关闭 Compose

curl http://localhost:8080/api/v1/ping
curl http://localhost:8080/actuator/health
```

> Compose 镜像默认走 DaoCloud 代理（`docker.m.daocloud.io`），便于国内拉取。
> MySQL/Redis 连通性测试：`InfrastructureConnectivityTests`（需先 `docker compose up -d`）。
> 运行日志：`bash/.run/`。

## 云服务器（无 Docker）

前提：服务器已安装 **JDK 21**，以及可连的 **MySQL / Redis**（本机安装或云托管均可）。

```bash
cp .env.example .env          # 修改 MYSQL_* / REDIS_* / 端口
./bash/run-server.sh          # 构建并启动 controller
./bash/run-server.sh --with-task
./bash/run-server.sh --skip-build   # 已有 jar 时跳过编译
./bash/run-server.sh status
./bash/run-server.sh stop
./bash/run-server.sh restart
```

## MyBatis Generator

手写 Mapper 已废弃，表结构变更后在 repository 模块重新生成：

```bash
# 需 MySQL 已建表（doc/sql）；JDBC 见 generatorJdbc.properties
./bash/mybatis-generate.sh
# 仅编译 repository
./mvnw -pl flydeer-struct-mind-repository -am compile
```

生成位置：`mysql.entity` / `mysql.mapper` / `resources/mapper`（会覆盖同名文件）。

项目已通过 [`.mvn/settings.xml`](.mvn/settings.xml) + [`.mvn/maven.config`](.mvn/maven.config) 默认使用阿里云公共仓库；直接执行 `./mvnw` 即可。

## 常用命令

```bash
./mvnw clean verify
./mvnw spotless:apply
./mvnw -pl flydeer-struct-mind-controller -am test
```
