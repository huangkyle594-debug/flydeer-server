# 21-图集服务-api

> 面向前端联调的接口说明。覆盖 `AtlasController`（结构化思维导图「首页」图集操作）。  
> 用户登录 / me / refresh **不在本服务实现**，复用用户中心：见 [12-用户服务-api.md](./12-用户服务-api.md)。  
> 错误码与 HTTP 映射见 [14-错误码.md](./14-错误码.md)。  
> 图（Graph）CRUD 另文约定；图集与图为一对多，**图集接口不返回下属图信息，也无「根图」概念**。

**Base URL（本地默认）**：`http://localhost:8080`  
**本服务前缀**：`/api/v1/struct-mind/atlases`  
**控制器**：`AtlasController`

> 风格说明：除标签列表为 **GET** 外，其余均为 **POST**，业务参数放在 JSON Body（非路径变量）。

---

## 1. 约定

### 1.1 统一响应信封

与用户服务一致：

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
| Access Token | 请求头 `Authorization: Bearer <accessToken>` | 写操作与个性化列表 / 详情 |
| Refresh Token | Cookie（用户中心签发，Path=`/api/v1/auth`） | 本服务不消费 |

Controller 通过 `@AuthCheck` 解析身份：

| 接口 | `required` | `resolve` | 说明 |
|---|---|---|---|
| 列表 `query` | 匿名（可选 Token） | `DELEGATE` | 有 Token 时解析 `userId`（及委托集合）；无 Token 仅看已发布 |
| 标签 `tags` | 匿名 | — | 无鉴权注解 |
| 创建 `create` | **已实名** `VERIFIED` | `SELF` | 未登录 401；未实名 403 |
| 更新 / 提交审核 / 删除 | **已实名** `VERIFIED` | `DELEGATE` | 业务侧仍要求 `authorId ==` Token 用户 |
| 详情 `detail` | 匿名（可选 Token） | `SELF` | 已发布全员可读；草稿/待审仅作者 |

未登录访问需登录接口 → **HTTP 401**（`code=31010`）。  
需实名而未 verified → **HTTP 403**（`code=31020`）。  
账户禁用（JWT `status`）→ **HTTP 500** body（`code=52010`）。

### 1.3 权限与可见范围

库表字段 `visible`（`TINYINT`，`1`=可见 / `0`=不可见）控制**对外发现**。开发阶段创建的图集一律 `visible=0`，对外列表/详情均不可见；作者仍可查看与管理自己的图集。

| 规则 | 说明 |
|---|---|
| 列表 `scope=ALL`（默认） | **`published` 且 `visible=1`** +（已登录时）**自己创建的**任意状态图集 |
| 列表 `CREATED` / `MANAGED` | 均要求已登录；当前实现均为「`authorId` = 当前用户」（协作未拆分）；不受 `visible` 限制 |
| 详情 | `published` 且 `visible=1`：任何人；否则仅作者 |
| `editable`（仅列表项） | `viewerId`（Token 用户）与 `authorId` 相等时为 `true` |
| 作者字段 | 创建时前端**不传**；由 Token 写入 `authorId`；`authorName` 当前写入空串 |
| 写操作作者校验 | `update` / `submit-review` / `delete`：`authorId` 必须等于 Token `userId`，否则 `ATLAS_FORBIDDEN` |

> Auth 层 `DELEGATE` 会填充 `allUserIds`（自己 + 已接受委托的被代理人），但**当前图集写操作与列表过滤尚未使用该集合**，仍以 Token `userId` 为准。

### 1.4 状态机（AtlasStatus）

状态由服务端流转，**前端不可手动指定 status**。

```
draft  --提交审核-->  pending  --人审通过-->  published
  ^                    |
  |---- 待审期间改动回滚 ----|
```

| 值 | 含义 |
|---|---|
| `draft` | 草稿：可编辑、可提交审核、可删除 |
| `pending` | 待人审；期间对元信息的改动 → 回滚为 `draft` |
| `published` | 已发布；对外可见还需 `visible=1`；作者仍可编辑（`pending` 时改动会回滚为 `draft`） |

人审通过为运营管理端操作：`POST /api/v1/admin/atlas/pending`（待审列表）、`POST /api/v1/admin/atlas/approve`（批准发布）。详见 [12-用户服务-api.md](./12-用户服务-api.md)「管理端接口」。

### 1.5 与图（Graph）的关系

- 图集 : 图 = **一对多**；图侧持有所属图集 ID（后续 Graph 服务实现）。
- **无根图（rootGraph）概念**。
- 本服务图集接口 **不返回** `graphIds` / `rootGraphId` / `graphs` 等下属图字段。
- **图集导入为纯前端功能**，**无后端接口**。

### 1.6 字段长度与分页上限

| 项 | 限制 |
|---|---|
| `name` | 必填（创建），最长 **64** |
| `description` | 最长 **500** |
| 单个 tag | 最长 **20**；服务端会 trim、去重、超长截断 |
| `page` | 默认 **1**（≤0 时按 1） |
| `pageSize` | 默认 **10**（≤0 时按 10），上限 **50** |

### 1.7 公共类型

**AtlasVO**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | string | 图集 ID（数值主键字符串，如 `"1"`） |
| `name` | string | 标题 |
| `description` | string | 简介，可空串 |
| `authorId` | string | 作者用户 ID（字符串） |
| `authorName` | string | 作者昵称快照（创建时当前可能为空串） |
| `status` | string | `draft` \| `pending` \| `published` |
| `tags` | string[] | 标签列表 |
| `createdAt` | long | 创建时间（ms） |
| `updatedAt` | long | 更新时间（ms） |

**AtlasListItemVO** = `AtlasVO` +：

| 字段 | 类型 | 说明 |
|---|---|---|
| `editable` | boolean | 当前请求者是否为作者 |

**AtlasPageVO**

| 字段 | 类型 | 说明 |
|---|---|---|
| `items` | AtlasListItemVO[] | 当前页 |
| `hasMore` | boolean | 是否还有下一页 |
| `total` | long | 命中总数 |

---

## 2. 接口一览

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/v1/struct-mind/atlases/query` | 匿名（可选 Token） | 分页列表 |
| GET | `/api/v1/struct-mind/atlases/tags` | 匿名 | 标签全集 |
| POST | `/api/v1/struct-mind/atlases/create` | 已实名 | 新建图集（`draft`） |
| POST | `/api/v1/struct-mind/atlases/update` | 已实名 + 作者 | 更新元信息 |
| POST | `/api/v1/struct-mind/atlases/submit-review` | 已实名 + 作者 | 提交审核 |
| POST | `/api/v1/struct-mind/atlases/delete` | 已实名 + 作者 | 删除图集 |
| POST | `/api/v1/struct-mind/atlases/detail` | 匿名（可选 Token） | 图集详情 |

---

## 3. 接口详情

### 3.1 图集列表（分页）

- **路由**：`POST /api/v1/struct-mind/atlases/query`
- **鉴权**：匿名；带 Token 时按登录用户计算可见范围与 `editable`
- **逻辑**：可见范围过滤 → `scope` 筛选 → keyword / tags → 按 `updated_at` 降序分页 → 填充 `editable`
- **注意**：`scope` 为 `CREATED` / `MANAGED` 且未登录 → **401**（`code=31010`）
- **Body**：可省略整个 body，等价于默认分页（`page=1`，`pageSize=10`，`scope=ALL`）

**Request Body**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `keyword` | string | 否 | 模糊匹配标题 / 简介 / 作者名 |
| `scope` | string | 否 | 见下表；默认 / 省略 = `ALL` |
| `tags` | string[] | 否 | 命中**任一**标签即保留（`tags LIKE`） |
| `page` | int | 否 | 从 1 起，默认 1 |
| `pageSize` | int | 否 | 默认 10，上限 50 |

**`scope` 枚举（AtlasPermissionScope）**

| 值 | 含义 | 过滤规则（当前实现） |
|---|---|---|
| `ALL`（默认） | 全部可见 | 未登录：仅 `published` 且 `visible=1`；已登录：同上 **或** `authorId`=自己 |
| `CREATED` | 我创建的 | 必须登录；`authorId` = 当前用户 |
| `MANAGED` | 我管理的 | 必须登录；当前与 `CREATED` 相同（协作未上线） |

```json
{
  "keyword": "",
  "scope": "CREATED",
  "tags": ["安全", "计算机"],
  "page": 1,
  "pageSize": 10
}
```

**Response `data`**：`AtlasPageVO`

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "items": [
      {
        "id": "1",
        "name": "密码学基础脉络",
        "description": "对称/非对称加密、散列、签名与证书体系。",
        "authorId": "10000001",
        "authorName": "",
        "status": "published",
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
  -d '{"page":1,"pageSize":10}'
```

---

### 3.2 标签全集

- **路由**：`GET /api/v1/struct-mind/atlases/tags`
- **鉴权**：匿名
- **逻辑**：返回配置项 `app.atlas.preset-tags`（`AtlasConfig.presetTags`），保持配置顺序
- **Body**：无

**预置标签**（`app.atlas.preset-tags`）：

`流程`、`系统`、`架构`、`鉴权`、`入门`、`进阶`、`计算机`、`调试`、`机器学习`、`Web`、`运维`、`数据库`、`网络`、`安全`

**Response `data`**：`string[]`

```json
{
  "code": 0,
  "message": "ok",
  "data": ["流程", "系统", "架构", "鉴权", "入门", "进阶", "计算机", "调试", "机器学习", "Web", "运维", "数据库", "网络", "安全"]
}
```

```bash
curl http://localhost:8080/api/v1/struct-mind/atlases/tags
```

---

### 3.3 新建图集

- **路由**：`POST /api/v1/struct-mind/atlases/create`
- **鉴权**：**已实名**（`VERIFIED`）
- **逻辑**：校验 body → Token 取 `authorId` → 创建 `status=draft` → 返回 `AtlasVO`
- **注意**：不创建图；客户端误传的作者字段会被忽略；`authorName` 当前固定写空串

**Request Body**

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| `name` | string | 是 | 非空，≤ 64 |
| `description` | string | 否 | 默认 `""`，≤ 500 |
| `tags` | string[] | 否 | 默认 `[]`；单项 ≤ 20 字 |

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

### 3.4 更新图集基本信息

- **路由**：`POST /api/v1/struct-mind/atlases/update`
- **鉴权**：**已实名**，且为作者
- **逻辑**：部分更新 `name` / `description` / `tags`；`tags` 为全量替换；若当前为 `pending` 且确有字段变更 → 回滚为 `draft`
- **注意**：未传任何可更新字段时直接返回现有数据；`name` 若传入则不可为空串

**Request Body**

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| `atlasId` | long | 是 | 图集 ID |
| `name` | string | 否 | 有则非空，≤ 64 |
| `description` | string | 否 | ≤ 500 |
| `tags` | string[] | 否 | 传入则全量替换 |

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
- **鉴权**：**已实名**，且为作者
- **逻辑**：仅 `draft` → `pending`；其它状态 → `40000`「仅草稿状态可提交审核」

**Request Body**

| 字段 | 类型 | 必填 |
|---|---|---|
| `atlasId` | long | 是 |

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
- **鉴权**：**已实名**，且为作者
- **逻辑**：删除图集行；下属图由后续 Graph 服务按 `atlasId` 清理（本期不级联）

**Request Body**

| 字段 | 类型 | 必填 |
|---|---|---|
| `atlasId` | long | 是 |

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

### 3.7 导入图集（纯前端，无后端接口）

- **不提供** `/atlases/import`。
- 前端自行：校验导出格式 → 重映射 ID → 写入 localStorage → 进入工作区。
- 本地导入图集 ID 可与服务端数值主键区分；首页列表由前端合并展示。

---

### 3.8 图集详情

- **路由**：`POST /api/v1/struct-mind/atlases/detail`
- **鉴权**：匿名可访问**已发布**；草稿 / 待审核仅作者（需带 Token）
- **逻辑**：返回 `AtlasVO`（**不含**下属图信息）
- **前端约定**：从首页列表 / 创建结果进入工作区时，可直接复用列表项或创建返回的元信息，不一定调用本接口。

**Request Body**

| 字段 | 类型 | 必填 |
|---|---|---|
| `atlasId` | long | 是 |

```json
{ "atlasId": 1 }
```

**Response `data`**：`AtlasVO`

```bash
curl -X POST http://localhost:8080/api/v1/struct-mind/atlases/detail \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1}'
```

---

## 4. 前端对接对照（相对旧文档 / mock）

| 旧习惯 | 当前实现（请改调用） |
|---|---|
| 前缀 `/api/struct-mind/v1/...` | **`/api/v1/struct-mind/atlases/...`** |
| `POST /tags/query` | **`GET /api/v1/struct-mind/atlases/tags`** |
| 创建 / 写操作仅需登录 | **需已实名 `VERIFIED`**（未绑手机的 OAuth 用户会 403） |
| `GET /atlases?...` | `POST .../atlases/query` + JSON body |
| `POST /atlases` | `POST .../atlases/create` |
| `PATCH /atlases/{id}` | `POST .../atlases/update`，`atlasId` 放 body |
| `POST /atlases/{id}/submit-review` | `POST .../atlases/submit-review` |
| `DELETE /atlases/{id}` | `POST .../atlases/delete` |
| `POST /atlases/import` | **无后端接口**（纯前端） |
| 响应含 `graphIds` / `rootGraphId` | **已移除**，勿再依赖 |
| 服务端图集 id | 数值主键字符串（如 `"1"`） |

用户身份仍走：`GET /api/v1/user/me`（见 [12-用户服务-api.md](./12-用户服务-api.md)）。

---

## 5. 相关错误码（图集）

| code | 含义 | HTTP（当前） | 典型场景 |
|---|---|---|---|
| `0` | 成功 | 200 | — |
| `31010` | 需要登陆态 | 401 | 写操作未登录；`scope=CREATED/MANAGED` 且未登录 |
| `31020` | 需要实名 | 403 | 写操作 Token 未 verified |
| `40000` | 请求不合法 | 400 | 参数校验失败；非草稿提交审核；非待审批准发布；更新时 name 为空串 |
| `51030` | 图集不存在 | 500 | `ATLAS_NOT_FOUND` |
| `52010` | 用户已禁用 | 500 | JWT `status` 非 ACTIVE |
| `52030` | 无权操作该图集 | 500 | `ATLAS_FORBIDDEN`（非作者；或详情不可见） |

> `ATLAS_*` / `USER_INVALID` 等业务异常当前全局映射多为 HTTP 500，前端请以 body 中的 `code` / `message` 为准（见 [14-错误码.md](./14-错误码.md)）。

---

## 6. 非目标（本文不覆盖）

- 图集导入 / 导出（纯前端）
- 图内容 CRUD（按 `atlasId` 挂载的一对多图服务）
- 人审驳回（当前仅支持批准发布）
- 委托用户代写图集（`allUserIds` 已解析，业务尚未接入）
