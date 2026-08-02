# 05-图集服务-api（首页图集操作）

> 面向前端 / 后端联调的接口说明。覆盖结构化思维导图「首页」所需的图集（Atlas）读写、标签、发布审核与导入。  
> **接口风格对齐** [`doc/05-flydeer-user-api.md`](../05-flydeer-user-api.md)（统一响应信封、Bearer 鉴权、curl 示例）。  
> 用户登录 / me / refresh **不在本服务实现**，复用用户中心：`/api/v1/auth/*`、`/api/v1/user/me`。  
> **暂不包含错误码清单**（尚未定稿）。图（Graph）节点级接口另文约定，本文仅列首页图集操作所需能力。

**Base URL（本地默认）**：`http://localhost:8080`  
**线上域名**：`www.fly-deer.com`  
**本服务前缀**：`/api/struct-mind/v1`

---

## 1. 约定

### 1.1 统一响应信封

与用户服务一致，均返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | int | `0` 表示成功；非 0 为业务/错误码（细节后续补充） |
| `message` | string | 提示文案 |
| `data` | object / array / null | 成功时的载荷；无数据时可为 `null` |

时间戳一律为**毫秒级 Unix 时间**（`number` / `long`）。

### 1.2 认证方式

| 凭证 | 传递方式 | 用途 |
|---|---|---|
| Access Token | 请求头 `Authorization: Bearer <accessToken>` | 访问需登录接口；与飞天小鹭主站同源共享 |
| Refresh Token | Cookie（用户中心签发，Path=`/api/v1/auth`） | 本服务不消费；前端经用户中心 `/api/v1/auth/refresh` 换发 Access |

控制器通过与用户服务相同的鉴权约定解析身份（如 `@AuthCheck`）：

| 接口类型 | 典型要求 |
|---|---|
| 公开读（已发布列表 / 标签） | 匿名可访问；若带 Token 则附加 `editable` 等个性化字段 |
| 写操作 / 仅自己可见的草稿与待审核 | 必须已登录（`AUTHENTICATED`） |

未登录访问受保护接口时，通常返回 **HTTP 401**。需要实名而 Token 未 `verified` 时为 **HTTP 403**（`NEED_VERIFY`）——首页写操作是否强制实名由产品定，**本文默认写操作仅要求已登录**。

### 1.3 权限与可见范围

| 规则 | 说明 |
|---|---|
| 可见范围 | **已发布**的全部图集 + **当前用户自己的**全部图集（含草稿 / 待审核） |
| `editable` | 由服务端按鉴权判定（作者本人可编辑）；前端据此展示发布 / 编辑 / 删除 |
| 作者字段 | 创建、导入时**前端不传** `authorId` / `authorName`，由服务端从 Token 解析并写入 |
| 未登录 | 仅可看已发布内容；`editable` 恒为 `false`；写操作 401 |

### 1.4 状态机（AtlasStatus）

状态由服务端流转，**前端不可手动指定 status**。

```
draft  --提交审核-->  pending  --人审通过-->  published
  ^                    |
  |---- 待审期间任一改动回滚 ----|
```

| 值 | 含义 |
|---|---|
| `draft` | 草稿：可编辑、可提交审核、可删除 |
| `pending` | 待人审：列表可见于作者；**期间对图集元信息或图内容的任何改动 → 回滚为 `draft`** |
| `published` | 已发布：对全员可见；作者仍可编辑（编辑后是否自动回草稿另议，**首页当前约定：仅 pending 回滚**） |

提交审核前，前端应二次确认文案：

> 申请发布后将进入人审阶段，期间任何改动都会导致图集回滚到草稿状态，确认现在提交吗？

人审通过为**服务端异步 / 运营操作**，无前端调用接口（或仅内部管理端，不在本文）。

### 1.5 公共响应类型

**AtlasStatus**：`draft` \| `pending` \| `published`

**AtlasVO**（图集主体）

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | string | 图集 ID，建议形如 `atl_xxx` |
| `name` | string | 标题 |
| `description` | string | 简介，可空串 |
| `authorId` | string | 作者用户 ID（与 `/api/v1/user/me` 的 `userId` 字符串形式一致，便于前端比对） |
| `authorName` | string | 作者昵称快照（创建/导入时写入；昵称变更是否回写另议） |
| `status` | string | 见状态枚举 |
| `tags` | string[] | 标签列表 |
| `graphIds` | string[] | 图集内图 ID 列表（有序） |
| `rootGraphId` | string | 根图 ID（新建时自动创建空根图） |
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
| `total` | long | 命中总数（筛选后） |

**AtlasCreateRequest**

| 字段 | 必填 | 规则 |
|---|---|---|
| `name` | 是 | 非空，建议 ≤ 64 |
| `description` | 否 | 默认 `""`，建议 ≤ 500 |
| `tags` | 否 | 默认 `[]`；单项建议 ≤ 20 字 |

**AtlasUpdateRequest**

| 字段 | 必填 | 规则 |
|---|---|---|
| `name` | 否 | 有则非空 |
| `description` | 否 | — |
| `tags` | 否 | 全量替换标签列表 |

**AtlasImportRequest**（与前端导出格式一致）

```json
{
  "format": "struct-mind/atlas",
  "version": 1,
  "atlas": { /* 原 Atlas 字段，id 仅作参考 */ },
  "graphs": [ /* Graph[] */ ]
}
```

导入时服务端须：重新生成全部 `id`；重映射 `graphIds`、`parentGraphId`、图节点 `data.graphId`；`authorId` / `authorName` 取当前用户；`status` 置为 `draft`。

---

## 2. Atlas — `/api/struct-mind/v1`

控制器建议名：`AtlasController`

除特别说明外，写操作均需：

```http
Authorization: Bearer <accessToken>
```

---

### 2.1 图集列表（分页）

- **路由**：`GET /api/struct-mind/v1/atlases`
- **鉴权**：匿名可访问；带 Token 时按登录用户计算可见范围与 `editable`
- **逻辑**：按可见范围过滤 → 应用筛选 → 按 `updatedAt` 降序分页 → 填充 `editable`
- **注意**：`editable=true` 时若未登录，应返回 401 或忽略该条件并视为无结果（推荐 **401**，与前端「可编辑筛选需登录」一致）

**Query**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `keyword` | string | 否 | 匹配标题 / 简介 / 作者名（模糊） |
| `editable` | boolean | 否 | `true` 时仅返回当前用户可编辑的图集 |
| `tags` | string | 否 | 逗号分隔；命中**任一**标签即保留 |
| `page` | int | 否 | 从 1 起，默认 1 |
| `pageSize` | int | 否 | 默认 10，建议上限 50 |

**Response `data`**：`AtlasPageVO`

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "items": [
      {
        "id": "atl_xxx",
        "name": "密码学基础脉络",
        "description": "对称/非对称加密、散列、签名与证书体系如何环环相扣。",
        "authorId": "10001",
        "authorName": "山泽",
        "status": "published",
        "tags": ["安全", "计算机"],
        "graphIds": ["gph_a"],
        "rootGraphId": "gph_a",
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
curl 'http://localhost:8080/api/struct-mind/v1/atlases?keyword=&editable=false&page=1&pageSize=10' \
  -H 'Authorization: Bearer <accessToken>'
```

---

### 2.2 标签全集

- **路由**：`GET /api/struct-mind/v1/tags`
- **鉴权**：匿名
- **逻辑**：返回预置标签 ∪ 数据中已出现的标签（去重）；供首页筛选区点选
- **注意**：排序可由服务端固定（如预置在前、其余字典序）

**Response `data`**：`string[]`

```json
{
  "code": 0,
  "message": "ok",
  "data": ["流程", "系统", "架构", "鉴权", "入门", "进阶", "安全", "计算机"]
}
```

```bash
curl 'http://localhost:8080/api/struct-mind/v1/tags'
```

---

### 2.3 新建图集

- **路由**：`POST /api/struct-mind/v1/atlases`
- **鉴权**：已登录
- **逻辑**：校验 body → 从 Token 取作者 → 创建图集（`status=draft`）→ **自动创建空根图** → 写入 `graphIds` / `rootGraphId` → 返回 `AtlasVO`
- **注意**：不要信任客户端传的作者字段；忽略若误传

**Request Body**：`AtlasCreateRequest`

```json
{
  "name": "我的架构笔记",
  "description": "分层与边界",
  "tags": ["架构", "入门"]
}
```

**Response `data`**：`AtlasVO`（新建时无 `editable` 字段也可；若带上则应为 `true`）

```bash
curl -X POST http://localhost:8080/api/struct-mind/v1/atlases \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"我的架构笔记","description":"分层与边界","tags":["架构","入门"]}'
```

---

### 2.4 更新图集基本信息

- **路由**：`PATCH /api/struct-mind/v1/atlases/{atlasId}`
- **鉴权**：已登录，且为作者（`editable`）
- **逻辑**：校验存在与权限 → 部分更新 `name` / `description` / `tags` → 刷新 `updatedAt`
- **注意**：
  - 若当前 `status=pending`，按状态机**回滚为 `draft`** 后再保存（或保存同时置 `draft`）
  - 非作者返回 403；不存在 404

**Request Body**：`AtlasUpdateRequest`

```json
{
  "name": "密码学基础脉络（修订）",
  "description": "补充证书链说明",
  "tags": ["安全", "计算机", "进阶"]
}
```

**Response `data`**：`AtlasVO`（或最新 `AtlasListItemVO`）

```bash
curl -X PATCH http://localhost:8080/api/struct-mind/v1/atlases/atl_xxx \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"密码学基础脉络（修订）","tags":["安全","计算机","进阶"]}'
```

---

### 2.5 提交审核（发布申请）

- **路由**：`POST /api/struct-mind/v1/atlases/{atlasId}/submit-review`
- **鉴权**：已登录，且为作者
- **逻辑**：仅当 `status=draft` 时可提交 → 置为 `pending` → 进入人审队列
- **注意**：
  - 非草稿状态提交应失败（如已是 pending / published）
  - 人审通过后由服务端置 `published`（无本接口）
  - 待审期间任意改动（含后续图内容写接口）须回滚 `draft`

**Request Body**：无（或空对象）

**Response `data`**：`null`（或更新后的 `AtlasVO`）

```bash
curl -X POST http://localhost:8080/api/struct-mind/v1/atlases/atl_xxx/submit-review \
  -H 'Authorization: Bearer <accessToken>'
```

---

### 2.6 删除图集

- **路由**：`DELETE /api/struct-mind/v1/atlases/{atlasId}`
- **鉴权**：已登录，且为作者
- **逻辑**：删除图集及其全部图（级联）；不可恢复
- **注意**：非作者 403；前端需二次确认后再调

**Response `data`**：`null`

```bash
curl -X DELETE http://localhost:8080/api/struct-mind/v1/atlases/atl_xxx \
  -H 'Authorization: Bearer <accessToken>'
```

---

### 2.7 导入图集

- **路由**：`POST /api/struct-mind/v1/atlases/import`
- **鉴权**：已登录
- **逻辑**：校验 `format=struct-mind/atlas` 且 `version=1` → 重映射 ID → 作者取当前用户 → `status=draft` → 落库全部图与图集 → 返回新 `AtlasVO`
- **注意**：导入体可能较大，建议限制体积（如 ≤ 5MB）；非法格式返回参数错误

**Request Body**：`AtlasImportRequest`

**Response `data`**：`AtlasVO`

```bash
curl -X POST http://localhost:8080/api/struct-mind/v1/atlases/import \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d @atlas-export.json
```

---

### 2.8 图集详情（首页进入工作区可选）

> 首页点击卡片进入工作区时前端目前可读本地 / 后续拉详情。若后端统一数据源，建议提供本接口。

- **路由**：`GET /api/struct-mind/v1/atlases/{atlasId}`
- **鉴权**：匿名可访问**已发布**；草稿 / 待审核仅作者可读
- **逻辑**：返回 `AtlasVO`（可附带图的元信息列表，**不含** nodes/edges）
- **注意**：无权限时 403；不存在 404

**Response `data`**：`AtlasVO`（可选扩展 `graphs: GraphMeta[]`）

```bash
curl http://localhost:8080/api/struct-mind/v1/atlases/atl_xxx \
  -H 'Authorization: Bearer <accessToken>'
```

---

## 3. 与前端对接对照

| 前端（`src/api/structMind.ts` / `mockApi`） | 本服务接口 |
|---|---|
| `fetchAtlasPage` | `GET /atlases` |
| `fetchTags` | `GET /tags` |
| `createAtlas` | `POST /atlases` |
| `updateAtlasInfo` | `PATCH /atlases/{atlasId}` |
| `submitForReview` | `POST /atlases/{atlasId}/submit-review` |
| `removeAtlas` | `DELETE /atlases/{atlasId}` |
| `importAtlas` | `POST /atlases/import` |

用户身份：`GET /api/v1/user/me`（用户中心，见 `doc/05-flydeer-user-api.md`）。  
前端 Access Token 存储键与主站一致：`localStorage['fd_access_token']`。

---

## 4. 非目标（本文不覆盖）

- 图内容 CRUD：`GET/PUT /graphs/{graphId}`、工作区内建图 / 删图等（见后续工作区接口文档）
- 导出下载专用端点（前端可先本地拼装 JSON；若需服务端导出可另加 `GET /atlases/{id}/export`）
- 人审管理端（通过 / 驳回）
- 错误码完整清单

---

## 5. 实现提示（给后端）

1. **路径不要 rewrite**：网关 / 本地代理保持 `/api/struct-mind/v1` 与 `/api/v1/auth` 原样，以免 Refresh Cookie Path 失效。
2. **`authorId` 类型**：建议存数值用户 ID，JSON 序列化为**字符串**，与前端 `String(userId)` 比对 `editable`。
3. **pending 回滚**：所有会修改图集或其下属图的写接口，开头统一检查：若 `pending` → 置 `draft` 再执行业务（或拒绝写并要求先撤回——产品已选「改动即回滚」）。
4. **列表性能**：首页无限滚动，注意 `tags` / `keyword` 索引；`total` 在大数据量下可做近似或上限。
