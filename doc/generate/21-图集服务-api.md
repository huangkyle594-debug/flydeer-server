# 21-图集服务-api

> 面向前端联调的接口说明。覆盖 `AtlasController`（结构化思维导图「首页」图集操作）。  
> 用户登录 / me / refresh **不在本服务实现**，复用用户中心：见 [12-用户服务-api.md](./12-用户服务-api.md)。  
> 图（Graph）CRUD 另文约定；图集与图为一对多，**图集接口不返回下属图信息，也无「根图」概念**。

**Base URL（本地默认）**：`http://localhost:8080`  
**本服务前缀**：`/api/struct-mind/v1`  
**控制器**：`AtlasController`

> 风格说明：与用户委托接口一致，**全部为 POST**，业务参数放在 JSON Body（非 REST 路径变量 / Query）。

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
| Access Token | 请求头 `Authorization: Bearer <accessToken>` | 写操作与个性化列表 |
| Refresh Token | Cookie（用户中心签发，Path=`/api/v1/auth`） | 本服务不消费 |

| 接口类型 | 鉴权 |
|---|---|
| 列表 / 标签 / 详情（公开读） | 匿名可访问；带 Token 时附加可见范围与 `editable` |
| 创建 / 更新 / 提交审核 / 删除 | 必须已登录（`AUTHENTICATED`）；写操作**不要求**实名 |

未登录访问受保护接口 → **HTTP 401**（`code=31010`）。

### 1.3 权限与可见范围

| 规则 | 说明 |
|---|---|
| 可见范围 | **已发布**的全部图集 + **当前用户自己的**图集（含草稿 / 待审核） |
| `editable` | 服务端判定：作者本人为 `true` |
| 作者字段 | 创建时**前端不传** `authorId` / `authorName`，由 Token 解析写入 |
| 未登录 | 仅可看已发布；`editable` 恒为 `false` |

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
| `pending` | 待人审；期间对图集元信息的改动 → 回滚为 `draft` |
| `published` | 已发布：全员可见；作者仍可编辑（当前约定：仅 `pending` 改动回滚） |

人审通过为运营 / 服务端操作，**无前端接口**。

### 1.5 与图（Graph）的关系

- 图集 : 图 = **一对多**；图侧持有所属图集 ID（后续 Graph 服务实现）。
- **无根图（rootGraph）概念**。
- 本服务图集接口 **不返回** `graphIds` / `rootGraphId` / `graphs` 等下属图字段。
- **图集导入为纯前端功能**（解析导出 JSON → 重映射 ID → 写入浏览器 localStorage），**无后端接口**。

### 1.6 公共类型

**AtlasVO**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | string | 图集 ID（数值主键的字符串形式，如 `"1"`） |
| `name` | string | 标题 |
| `description` | string | 简介，可空串 |
| `authorId` | string | 作者用户 ID（字符串，便于与 `String(userId)` 比对） |
| `authorName` | string | 作者昵称快照 |
| `status` | string | `draft` \| `pending` \| `published` |
| `tags` | string[] | 标签列表 |
| `createdAt` | long | 创建时间（ms） |
| `updatedAt` | long | 更新时间（ms） |

**AtlasListItemVO** = `AtlasVO` +：

| 字段 | 类型 | 说明 |
|---|---|---|
| `editable` | boolean | 当前请求者是否可编辑 |

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
| POST | `/api/struct-mind/v1/atlases/query` | 匿名（可选 Token） | 分页列表 |
| POST | `/api/struct-mind/v1/tags/query` | 匿名 | 标签全集 |
| POST | `/api/struct-mind/v1/atlases/create` | 登录 | 新建图集 |
| POST | `/api/struct-mind/v1/atlases/update` | 登录 + 作者 | 更新元信息 |
| POST | `/api/struct-mind/v1/atlases/submit-review` | 登录 + 作者 | 提交审核 |
| POST | `/api/struct-mind/v1/atlases/delete` | 登录 + 作者 | 删除图集 |
| POST | `/api/struct-mind/v1/atlases/detail` | 匿名（可选 Token） | 图集详情（首页打开工作区**不调用**；列表项已含元信息） |

---

## 3. 接口详情

### 3.1 图集列表（分页）

- **路由**：`POST /api/struct-mind/v1/atlases/query`
- **鉴权**：匿名；带 Token 时按登录用户计算可见范围与 `editable`
- **逻辑**：可见范围过滤 → 按 `scope` 筛选 → 其它筛选 → 按 `updatedAt` 降序分页 → 填充 `editable`
- **注意**：`scope` 为 `CREATED` / `MANAGED` 且未登录 → **401**（`code=31010`）

**Request Body**（可省略整个 body，等价于默认分页）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `keyword` | string | 否 | 模糊匹配标题 / 简介 / 作者名 |
| `scope` | string | 否 | 权限筛选，见下表；默认 / 省略 / `ALL` = 不额外按权限收窄 |
| `tags` | string[] | 否 | 命中**任一**标签即保留 |
| `page` | int | 否 | 从 1 起，默认 1 |
| `pageSize` | int | 否 | 默认 10，上限 50 |

**`scope` 枚举**

| 值 | 含义 | 过滤规则 |
|---|---|---|
| `ALL`（默认） | 全部 | 仅应用既有可见范围（已发布 + 自己的草稿/待审等），不按权限再滤 |
| `CREATED` | 我创建的 | `authorId` = 当前用户 |
| `MANAGED` | 我管理的 | 当前用户对图集有管理/编辑权限；协作未上线前等价于「作者为当前用户」 |

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
        "authorId": "10001",
        "authorName": "山泽",
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
curl -X POST http://localhost:8080/api/struct-mind/v1/atlases/query \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"page":1,"pageSize":10}'
```

---

### 3.2 标签全集

- **路由**：`POST /api/struct-mind/v1/tags/query`
- **鉴权**：匿名
- **逻辑**：预置标签 ∪ 库中已出现标签（去重）；预置在前，其余字典序
- **Body**：无（可不传）

**Response `data`**：`string[]`

```json
{
  "code": 0,
  "message": "ok",
  "data": ["流程", "系统", "架构", "鉴权", "入门", "进阶", "计算机", "调试", "机器学习", "Web", "运维", "数据库", "网络", "安全"]
}
```

```bash
curl -X POST http://localhost:8080/api/struct-mind/v1/tags/query \
  -H 'Content-Type: application/json'
```

---

### 3.3 新建图集

- **路由**：`POST /api/struct-mind/v1/atlases/create`
- **鉴权**：已登录
- **逻辑**：校验 body → Token 取作者 → 创建 `status=draft` 图集 → 返回 `AtlasVO`
- **注意**：不创建图；忽略客户端误传的作者字段

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
curl -X POST http://localhost:8080/api/struct-mind/v1/atlases/create \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"我的架构笔记","description":"分层与边界","tags":["架构","入门"]}'
```

---

### 3.4 更新图集基本信息

- **路由**：`POST /api/struct-mind/v1/atlases/update`
- **鉴权**：已登录，且为作者
- **逻辑**：部分更新 `name` / `description` / `tags`；`tags` 为全量替换；`pending` → 回滚 `draft`
- **注意**：非作者业务错误（`ATLAS_FORBIDDEN`）；不存在（`ATLAS_NOT_FOUND`）

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
curl -X POST http://localhost:8080/api/struct-mind/v1/atlases/update \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1,"name":"密码学基础脉络（修订）","tags":["安全","计算机","进阶"]}'
```

---

### 3.5 提交审核

- **路由**：`POST /api/struct-mind/v1/atlases/submit-review`
- **鉴权**：已登录，且为作者
- **逻辑**：仅 `draft` → `pending`；其它状态失败

**Request Body**

| 字段 | 类型 | 必填 |
|---|---|---|
| `atlasId` | long | 是 |

```json
{ "atlasId": 1 }
```

**Response `data`**：`null`

```bash
curl -X POST http://localhost:8080/api/struct-mind/v1/atlases/submit-review \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1}'
```

---

### 3.6 删除图集

- **路由**：`POST /api/struct-mind/v1/atlases/delete`
- **鉴权**：已登录，且为作者
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
curl -X POST http://localhost:8080/api/struct-mind/v1/atlases/delete \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1}'
```

---

### 3.7 导入图集（纯前端，无后端接口）

- **不提供** `/atlases/import`。
- 前端自行：校验 `format=struct-mind/atlas` + `version=1` → 重映射图集/图 ID → 写入 localStorage → 进入工作区。
- 本地导入图集 ID 形如 `atl_xxx`，与服务端数值主键区分；首页列表由前端合并展示。

---

### 3.8 图集详情

- **路由**：`POST /api/struct-mind/v1/atlases/detail`
- **鉴权**：匿名可访问**已发布**；草稿 / 待审核仅作者
- **逻辑**：返回 `AtlasVO`（**不含**下属图信息）
- **前端约定**：从首页列表 / 创建结果进入工作区时，**不要调用本接口**；直接复用列表项或创建返回的元信息即可。

**Request Body**

| 字段 | 类型 | 必填 |
|---|---|---|
| `atlasId` | long | 是 |

```json
{ "atlasId": 1 }
```

**Response `data`**：`AtlasVO`

```bash
curl -X POST http://localhost:8080/api/struct-mind/v1/atlases/detail \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1}'
```

---

## 4. 前端对接对照（相对旧 mock / 初稿）

| 前端旧习惯 | 本服务（请改调用） |
|---|---|
| `GET /atlases?...` | `POST /atlases/query` + JSON body |
| `GET /tags` | `POST /tags/query` |
| `POST /atlases` | `POST /atlases/create` |
| `PATCH /atlases/{id}` | `POST /atlases/update`，`atlasId` 放 body |
| `POST /atlases/{id}/submit-review` | `POST /atlases/submit-review`，`atlasId` 放 body |
| `DELETE /atlases/{id}` | `POST /atlases/delete`，`atlasId` 放 body |
| `POST /atlases/import` | **删除**：改为纯前端 localStorage 导入 |
| `GET /atlases/{id}`（打开工作区） | **不请求**；用列表/创建/导入结果经路由 state 传入工作区 |
| 响应含 `graphIds` / `rootGraphId` | **已移除**，勿再依赖 |
| 服务端图集 id | 数值主键字符串（如 `"1"`）；本地导入仍可用 `atl_xxx` |
| `tags` Query 逗号分隔 | Body 中 `tags: string[]` |

用户身份仍走：`GET /api/v1/user/me`（见 [12-用户服务-api.md](./12-用户服务-api.md)）。  
Access Token 存储键与主站一致：`localStorage['fd_access_token']`。

---

## 5. 相关错误码（图集）

| code | 含义 | 典型场景 |
|---|---|---|
| `0` | 成功 | — |
| `31010` | 需要登陆态 | 写操作未登录；`scope` 为 `CREATED` / `MANAGED` 且未登录 |
| `40000` | 请求不合法 | 参数校验失败；非草稿提交审核 |
| `51030` | 图集不存在 | `ATLAS_NOT_FOUND` |
| `52030` | 无权操作该图集 | `ATLAS_FORBIDDEN`（非作者等） |

> 业务异常当前全局映射多为 HTTP 500，前端请以 body 中的 `code` / `message` 为准（与用户服务一致，见 [14-错误码.md](./14-错误码.md)）。

---

## 6. 非目标（本文不覆盖）

- 图集导入 / 导出（纯前端）
- 图内容 CRUD（按 `atlasId` 挂载的一对多图服务）
- 人审管理端（通过 / 驳回）
- 网关路径 rewrite（须保持 `/api/struct-mind/v1` 与 `/api/v1/auth` 原样，以免 Refresh Cookie Path 失效）
