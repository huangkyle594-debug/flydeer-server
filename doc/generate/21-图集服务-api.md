# 21-图集服务-api

> 面向前端联调的接口说明。覆盖 `AtlasController`（用户侧图集）与 `AdminController` 中的图集人审接口。  
> 登录 / me / refresh 见 [12-用户服务-api.md](./12-用户服务-api.md)。  
> 错误码与 HTTP 映射见 [14-错误码.md](./14-错误码.md)。  
> 图（Graph）CRUD 另文约定；图集与图为一对多，**本服务不返回下属图信息，也无「根图」概念**。

**Base URL（本地默认）**：`http://localhost:8080`  
**用户侧前缀**：`/api/v1/struct-mind/atlases`  
**管理端前缀**：`/api/v1/admin/atlas`

> 风格：除标签列表为 **GET** 外，其余均为 **POST**，业务参数放在 JSON Body。

---

## 1. 约定

### 1.1 统一响应信封

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | int | `0` 成功；非 0 为业务错误码 |
| `message` | string | 提示文案 |
| `data` | object / array / null | 成功载荷；无数据时可为 `null` |

时间戳为**毫秒级 Unix 时间**（`number` / `long`）。

### 1.2 认证方式

| 凭证 | 传递方式 | 用途 |
|---|---|---|
| Access Token | 请求头 `Authorization: Bearer <accessToken>` | 写操作、个性化列表、管理端 |
| Refresh Token | Cookie（用户中心签发） | 本服务不消费 |

| 接口 | `required` | `resolve` | 说明 |
|---|---|---|---|
| 列表 `query` | 匿名（可选 Token） | `DELEGATE` | 有 Token 时解析 `userId` / `allUserIds`；`CREATED`/`MANAGED` 必须登录 |
| 标签 `tags` | 匿名 | — | 无鉴权 |
| 创建 `create` | `VERIFIED` | `SELF` | 未登录 401；未实名 403 |
| 更新 / 提交审核 / 删除 | `VERIFIED` | `DELEGATE` | `authorId` 须在 `allUserIds` 内，否则无权 |
| 管理端待审 / 批准 | `ADMIN` | `SELF` | 用户 ID ∈ `app.user.admin-ids` |

### 1.3 可见范围与列表 scope

库表 `visible`（`1`=可见 / `0`=隐藏）控制**对外发现**。新建图集默认 `visible=0`；管理员批准发布后置 `visible=1`。

| `scope` | 含义 | 过滤规则 |
|---|---|---|
| `PUBLISHED`（默认 / 省略） | 公开列表 | `status=PUBLISHED` 且 `visible=1` |
| `CREATED` | 我创建的 | 须登录；`author_id = 当前用户`；**任意 status**，不受 `visible` 限制 |
| `MANAGED` | 我管理的 | 须登录；`author_id ∈ allUserIds`（自己 + 已接受委托的被代理人）；**任意 status**，不受 `visible` 限制 |

`editable`：当前请求的 `allUserIds` 包含该图集 `authorId` 时为 `true`。

### 1.4 状态机（AtlasStatus）

状态由服务端流转，**前端不可手动指定 status**。响应中的取值为枚举名：

```
DRAFT  --提交审核-->  PENDING  --管理员批准-->  PUBLISHED
```

| 值 | 含义 |
|---|---|
| `DRAFT` | 草稿：可编辑、可提交审核、可删除 |
| `PENDING` | 待人审：作者仍可编辑 / 删除；对外不可见 |
| `PUBLISHED` | 已发布；对外可见还需 `visible=1`（批准时一并打开） |

### 1.5 与图（Graph）的关系

- 图集 : 图 = **一对多**（图侧持有 `atlasId`，另文实现）。
- **无根图概念**；图集接口不返回下属图字段。
- **图集导入 / 导出为纯前端功能**，无后端接口。

### 1.6 字段与分页限制

| 项 | 限制 |
|---|---|
| `name` | 创建必填，最长 **64** |
| `description` | 最长 **500** |
| 单个 tag | 最长 **20**；服务端 trim、去重、超长截断 |
| `page` | 默认 **1**，校验上限 100 |
| `pageSize` | 默认 **10**，校验上限 100 |
| `orderBy` | 默认 `id`（列名由服务端使用，建议 `id` / `updated_at` / `created_at`） |
| `isAsc` | 默认 `true` |

### 1.7 公共类型

**AtlasVO**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | number | 图集 ID |
| `name` | string | 标题 |
| `description` | string | 简介，可空串 |
| `authorId` | number | 作者用户 ID |
| `authorName` | string | 作者昵称快照（来自 JWT `name`；用户更名后异步同步） |
| `status` | string | `DRAFT` \| `PENDING` \| `PUBLISHED` |
| `tags` | string[] | 标签 |
| `createdAt` | number | 创建时间（ms） |
| `updatedAt` | number | 更新时间（ms） |
| `editable` | boolean | 当前请求者是否可编辑（`authorId ∈ allUserIds`） |

**PageVO\<AtlasVO\>**

| 字段 | 类型 | 说明 |
|---|---|---|
| `list` | AtlasVO[] | 当前页 |
| `hasMore` | boolean | 是否还有下一页 |
| `total` | number | 命中总数 |
| `page` / `pageSize` | number | 可选；列表接口当前可能未回填 |

---

## 2. 接口一览

### 2.1 用户侧

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/v1/struct-mind/atlases/query` | 匿名（可选 Token） | 分页列表 |
| GET | `/api/v1/struct-mind/atlases/tags` | 匿名 | 预置标签 |
| POST | `/api/v1/struct-mind/atlases/create` | 已实名 | 新建（`DRAFT`） |
| POST | `/api/v1/struct-mind/atlases/update` | 已实名 + 可编辑 | 更新元信息 |
| POST | `/api/v1/struct-mind/atlases/submit-review` | 已实名 + 可编辑 | 提交审核 |
| POST | `/api/v1/struct-mind/atlases/delete` | 已实名 + 可编辑 | 删除图集 |

### 2.2 管理端

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/v1/admin/atlas/pending` | ADMIN | 待发布列表 |
| POST | `/api/v1/admin/atlas/approve` | ADMIN | 批准发布 |

---

## 3. 用户侧接口

### 3.1 图集列表

- **路由**：`POST /api/v1/struct-mind/atlases/query`
- **鉴权**：匿名；`scope=CREATED` / `MANAGED` 时必须登录，否则 **401**（`31010`）
- **Body**：`PageRequest`；`query` 建议始终传入（可为空对象）

**Request Body**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `query` | object | 是（校验） | 查询条件 |
| `query.keyword` | string | 否 | 模糊匹配 `name` / `description` / `author_name` |
| `query.scope` | string | 否 | `PUBLISHED` \| `CREATED` \| `MANAGED`；省略按公开列表 |
| `query.tags` | string[] | 否 | 命中**任一**标签（`JSON_OVERLAPS`） |
| `page` | int | 否 | 默认 1 |
| `pageSize` | int | 否 | 默认 10 |
| `orderBy` | string | 否 | 默认 `id` |
| `isAsc` | boolean | 否 | 默认 `true` |

```json
{
  "query": {
    "keyword": "",
    "scope": "CREATED",
    "tags": ["安全", "计算机"]
  },
  "page": 1,
  "pageSize": 10,
  "orderBy": "updated_at",
  "isAsc": false
}
```

**Response `data`**：`PageVO<AtlasVO>`

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "密码学基础脉络",
        "description": "对称/非对称加密、散列、签名与证书体系。",
        "authorId": 10000001,
        "authorName": "飞鹿用户",
        "status": "PUBLISHED",
        "tags": ["安全", "计算机"],
        "createdAt": 1754067780000,
        "updatedAt": 1754067780000,
        "editable": false
      }
    ],
    "hasMore": true,
    "total": 42
  }
}
```

```bash
curl -X POST http://localhost:8080/api/v1/struct-mind/atlases/query \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"query":{"scope":"CREATED"},"page":1,"pageSize":10,"orderBy":"updated_at","isAsc":false}'
```

---

### 3.2 预置标签

- **路由**：`GET /api/v1/struct-mind/atlases/tags`
- **鉴权**：匿名
- **逻辑**：返回配置 `app.atlas.tags`（顺序与配置一致）

默认预置：`流程`、`系统`、`架构`、`鉴权`、`入门`、`进阶`、`计算机`、`调试`、`机器学习`、`Web`、`运维`、`数据库`、`网络`、`安全`

**Response `data`**：`string[]`

```bash
curl http://localhost:8080/api/v1/struct-mind/atlases/tags
```

---

### 3.3 新建图集

- **路由**：`POST /api/v1/struct-mind/atlases/create`
- **鉴权**：已实名（`VERIFIED`）
- **逻辑**：`status=DRAFT`、`visible=0`；`authorId` / `authorName` 取自 Token（`userId` / `name`）

**Request Body**

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| `name` | string | 是 | 非空，≤ 64 |
| `description` | string | 否 | ≤ 500；建议传 `""`，避免省略为 null |
| `tags` | string[] | 否 | 默认 `[]`；单项 ≤ 20 |

```json
{
  "name": "我的架构笔记",
  "description": "分层与边界",
  "tags": ["架构", "入门"]
}
```

**Response `data`**：`AtlasVO`

```bash
curl -X POST http://localhost:8080/api/v1/struct-mind/atlases/create \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"我的架构笔记","description":"分层与边界","tags":["架构","入门"]}'
```

---

### 3.4 更新图集

- **路由**：`POST /api/v1/struct-mind/atlases/update`
- **鉴权**：已实名，且 `authorId ∈ allUserIds`
- **逻辑**：部分更新；`tags` 传入且非空时为全量替换；未传任何可更新字段则直接返回现有数据

**Request Body**

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| `atlasId` | number | 是 | 图集 ID |
| `name` | string | 否 | 有文本才更新，≤ 64 |
| `description` | string | 否 | 有文本才更新，≤ 500 |
| `tags` | string[] | 否 | 非空列表时全量替换 |

```json
{
  "atlasId": 1,
  "name": "密码学基础脉络（修订）",
  "description": "补充证书链说明",
  "tags": ["安全", "计算机", "进阶"]
}
```

**Response `data`**：`AtlasVO`

```bash
curl -X POST http://localhost:8080/api/v1/struct-mind/atlases/update \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1,"name":"密码学基础脉络（修订）","tags":["安全","计算机","进阶"]}'
```

---

### 3.5 提交审核

- **路由**：`POST /api/v1/struct-mind/atlases/submit-review`
- **鉴权**：已实名，且可编辑
- **逻辑**：仅 `DRAFT` → `PENDING`；其它状态 → `42010`

**Request Body**

```json
{ "atlasId": 1 }
```

**Response `data`**：`null`

```bash
curl -X POST http://localhost:8080/api/v1/struct-mind/atlases/submit-review \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1}'
```

---

### 3.6 删除图集

- **路由**：`POST /api/v1/struct-mind/atlases/delete`
- **鉴权**：已实名，且可编辑
- **逻辑**：删除图集行；下属图由 Graph 服务按 `atlasId` 清理（本期不级联）

**Request Body**

```json
{ "atlasId": 1 }
```

**Response `data`**：`null`

```bash
curl -X POST http://localhost:8080/api/v1/struct-mind/atlases/delete \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1}'
```

---

## 4. 管理端接口

均要求当前用户在 `app.user.admin-ids` 中，否则 **HTTP 403**（`31030`）。

### 4.1 待发布列表

- **路由**：`POST /api/v1/admin/atlas/pending`
- **逻辑**：分页查询 `status=PENDING`（不受 `visible` 限制）；可附带 `keyword` / `tags` / 分页参数
- **注意**：服务端强制 `status=PENDING`

**Request Body**（可省略）

```json
{
  "query": {
    "keyword": "架构"
  },
  "page": 1,
  "pageSize": 20,
  "orderBy": "updated_at",
  "isAsc": false
}
```

**Response `data`**：`PageVO<AtlasVO>`

```bash
curl -X POST http://localhost:8080/api/v1/admin/atlas/pending \
  -H 'Authorization: Bearer <adminAccessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"page":1,"pageSize":20,"orderBy":"updated_at","isAsc":false}'
```

---

### 4.2 批准发布

- **路由**：`POST /api/v1/admin/atlas/approve`
- **逻辑**：仅 `PENDING` → `PUBLISHED`，并置 `visible=1`；非待审 → `42030`

**Request Body**

```json
{ "atlasId": 1 }
```

**Response `data`**：`null`

```bash
curl -X POST http://localhost:8080/api/v1/admin/atlas/approve \
  -H 'Authorization: Bearer <adminAccessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1}'
```

---

## 5. 相关错误码

| code | 含义 | HTTP | 典型场景 |
|---|---|---|---|
| `0` | 成功 | 200 | — |
| `31010` | 需要登陆态 | 401 | 写操作未登录；`CREATED`/`MANAGED` 未登录 |
| `31020` | 需要实名 | 403 | Token 未 verified |
| `31030` | 需要管理员 | 403 | 非管理员访问管理端 |
| `40000` | 请求不合法 | 400 | `@Valid` / 参数校验失败 |
| `42010` | 仅草稿可提交审核 | 400 | 非 `DRAFT` 提交审核 |
| `42020` | 图集未发布 | 400 | 非作者查看未发布图集（详情类能力） |
| `42030` | 仅待审可批准发布 | 400 | 非 `PENDING` 批准 |
| `52010` | 用户已禁用 | 500 | JWT `status` 非 ACTIVE |
| `52030` | 图集不存在 / 无权操作 | 500 | `ATLAS_NOT_FOUND` / `ATLAS_FORBIDDEN` |
| `52040` | 图集不可见 | 500 | `ATLAS_NOT_VISIBLE` |

完整映射见 [14-错误码.md](./14-错误码.md)。业务异常请以 body 中的 `code` / `message` 为准。

---

## 6. 非目标（本文不覆盖）

- 图集导入 / 导出（纯前端）
- 图内容 CRUD
- 人审驳回（当前仅支持批准发布）
