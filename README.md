# FlyDeer Server

Spring Boot 多模块后端：JDK 21 · Spring Boot 3.5 · Maven · MyBatis · PageHelper · MySQL · Redis。

当前已落地能力以**用户服务**为主：手机号短信登录、Gitee / GitHub OAuth、资料与绑手机、委托代理。

## 文档

生成文档位于 [`doc/generate/`](doc/generate/)（由阶段需求梳理输出，随代码演进维护）：

| 文档 | 内容 |
|---|---|
| [00-项目概要](doc/generate/00-项目概要.md) | 目录结构、模块职责、外部依赖、可配置项、热更新 |
| [11-用户服务-业务流程](doc/generate/11-用户服务-业务流程.md) | 登录方式与业务主流程（不含实现细节） |
| [12-用户服务-api](doc/generate/12-用户服务-api.md) | Auth / User / Delegate 接口说明（面向前端联调） |
| [13-用户服务-技术方案](doc/generate/13-用户服务-技术方案.md) | 开发规范、本地/远程流程、表结构与业务用法 |
| [14-错误码](doc/generate/14-错误码.md) | ErrorCodes + GlobalExceptionHandler 错误码与 HTTP 映射 |

建表 SQL：[`doc/sql/02-user-service.sql`](doc/sql/02-user-service.sql)。

## 模块依赖

```
common  ←  contract  ←  repository  ←  service  ←  api  ←  controller  (HTTP Boot)
                                    ↘
                                      task  (定时任务 Boot，不依赖 api)
```

| 模块 | Artifact | 职责 |
|---|---|---|
| common | `flydeer-common` | 工具、异常、统一响应、Jackson |
| contract | `flydeer-contract` | DTO / 枚举 / 跨模块接口 |
| repository | `flydeer-repository` | MyBatis Mapper / Entity / PageHelper |
| service | `flydeer-service` | 可复用业务逻辑、事务、Redis、JWT、短信 |
| api | `flydeer-api` | 面向业务的用例编排 |
| task | `flydeer-task` | 独立定时任务应用（端口 8081） |
| controller | `flydeer-controller` | HTTP API + 可运行 Boot 应用（端口 8080） |

规则：上层依赖下层；禁止反向依赖（如 repository 不得依赖 service）。controller 依赖 api；task 只依赖 service。

## 快速开始（Docker）

```bash
./bash/run.sh                 # 构建并启动 MySQL + Redis + controller
./bash/run.sh stop            # 仅停 app
./bash/run.sh down            # 停全部容器（保留数据卷）
./bash/run.sh down -v         # 停全部并清空数据卷

curl http://localhost:8080/actuator/health
```

| 服务 | 地址 | 说明 |
|---|---|---|
| App | `http://localhost:8080` | controller 容器 |
| MySQL | `localhost:3306` | 库名 / 用户 / 密码均为 `flydeer` |
| Redis | `localhost:6379` | AOF 持久化 |

建表 SQL 在首次初始化时自动执行（`doc/sql` → MySQL `docker-entrypoint-initdb.d`）。  
镜像默认走 DaoCloud 代理；短信默认 `SMS_MOCK_ENABLED=true`。

## 云服务器（无 Docker）

前提：服务器已安装 **JDK 21**，以及可连的 **MySQL / Redis**（本机安装或云托管均可）。

```bash
cp .env.example .env          # 修改 MYSQL_* / REDIS_* / 端口，并配置 JWT / OAuth / 短信等
./bash/run-server.sh          # 构建并启动 controller
./bash/run-server.sh --with-task
./bash/run-server.sh --skip-build   # 已有 jar 时跳过编译
./bash/run-server.sh status
./bash/run-server.sh stop
./bash/run-server.sh restart
```

## 用户服务 API 速览

| 前缀 | 说明 |
|---|---|
| `/api/v1/auth/**` | 短信登录、OAuth、刷新、登出 |
| `/api/v1/user/**` | 资料、绑手机 |
| `/api/v1/user/delegate/**` | 委托代理 |

完整字段与调用约定见 [12-用户服务-api](doc/generate/12-用户服务-api.md)。受保护接口需头：`Authorization: Bearer <accessToken>`；Refresh Token 走 HttpOnly Cookie。

## MyBatis Generator

手写 Mapper 已废弃，表结构变更后在 repository 模块重新生成：

```bash
# 需 MySQL 已建表（doc/sql）；JDBC 见 generatorJdbc.properties
./bash/mybatis-generate.sh
# 仅编译 repository
./mvnw -pl flydeer-repository -am compile
```

生成位置：`mysql.entity` / `mysql.mapper` / `resources/mapper`（会覆盖同名文件）。

项目已通过 [`.mvn/settings.xml`](.mvn/settings.xml) + [`.mvn/maven.config`](.mvn/maven.config) 默认使用阿里云公共仓库；直接执行 `./mvnw` 即可。

## 常用命令

```bash
./mvnw clean verify
./mvnw spotless:apply
./mvnw -pl flydeer-controller -am test
```
