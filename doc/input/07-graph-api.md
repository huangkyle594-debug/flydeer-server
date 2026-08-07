# 07-图服务-api（后端开发文档）

> 承接 [`doc/21-图集服务-api.md`](../doc/21-图集服务-api.md) 中「图（Graph）CRUD 另文约定」的留白，给出图服务的接口列表、语义、数据模型与技术选型。
> 节点体系的设计前提见 [`doc-plan/06-nodes.md`](./06-nodes.md)；工作区的保存与目录交互见 [`doc-plan/04-workplace-up.md`](./04-workplace-up.md)。
> **接口风格对齐已实现的图集服务**（`doc/21`）：统一响应信封、Bearer 鉴权、除个别读接口外一律 POST + JSON Body。

**Base URL（本地默认）**：`http://localhost:8080`
**本服务前缀**：`/api/v1/struct-mind/graphs`

---

## 1. 定位与范围

图服务负责**图集内单张图的内容与目录位置**。图集元信息、发布审核、标签由图集服务负责，本服务不重复实现。

| 归属 | 能力 |
|---|---|
| 图集服务（已实现） | 图集 CRUD、标签、状态机、人审、权限判定的数据源 |
| **图服务（本文）** | 图的增删改查、全量内容保存、目录位置（父子关系）、批量保存 |
| 前端 | 目录树推导、面包屑、脏状态、导入导出、节点渲染与编辑 |

一句话概括本服务的职责：**它是一个带目录结构的图文档存储，不理解图的内容。**

---

## 2. 技术选型

### 2.1 技术栈

同工程内新增模块，不独立部署；应用层与图集服务同构，**存储层分库**：

| 层 | 选型 | 说明 |
|---|---|---|
| 语言 / 框架 | Java 17 + Spring Boot 3 | 与图集服务同工程 |
| Web | Spring MVC + `@Valid` | 参数校验沿用 `40000` 错误码 |
| 持久层 | MyBatis-Plus（第二个 DataSource） | 沿用 `@TableLogic` 逻辑删除与列名约定；JSONB 需自定义 TypeHandler，见 2.6 |
| **数据库** | **PostgreSQL 16 + JSONB** | 图专用库；图集仍在 MySQL 8，两库并存 |
| JSON | Jackson | 图内容以 `JsonNode` / 原始字符串读写，见 2.5 |
| 鉴权 | 复用用户中心 JWT + `@AuthCheck` | 与图集服务同一套注解与解析逻辑 |

不引入 MongoDB 等文档库：图内容确实是文档，但**目录结构、权限归属、批量查询都是关系型的**，为一个 JSON 字段换一套无关系能力的存储不划算。PostgreSQL 同时提供关系表与一流的文档列，是这个形状的数据最合适的落点。

### 2.2 为什么图用 PostgreSQL，而图集留在 MySQL

图集服务已经在 MySQL 上跑起来了（`doc/21`），只有一张表、JSON 用法仅限 `tags` 的 `JSON_OVERLAPS`，没有迁移动机。图是新表、且内容天生是文档，选型可以独立决定。

**PostgreSQL 的价值在于 JSONB 的可查询性不需要预先声明路径。** MySQL 8 也能索引 JSON 数组（8.0.17+ 的 multi-valued index），但每条想查的路径都要单独建索引、查询还必须用与索引定义完全一致的路径表达式。而本项目的节点 schema 是**刻意开放**的（`data.style` 为任意键值字典，能力字段可以随时新增，见 `06-nodes.md` 第 2 节）——「查询路径必须提前声明」与「schema 可以自由生长」是直接冲突的。PostgreSQL 一条 GIN 索引覆盖全部路径，`content @> '{...}'` 可以对任意字段做即席查询。

**但本期不建 GIN 索引，也不用这个能力。** 后端仍然把 `content` 当作不透明 blob（2.4、2.5）。选 PostgreSQL 是**为将来留门**：哪天要把外部指向的反查、服务端跨图节点搜索加回来，只需要 `CREATE INDEX ... USING GIN`，不需要迁库、不需要改表结构。

**双库的代价必须认下来**，不要假装没有：

| 代价 | 影响 |
|---|---|
| 两套连接池 / 备份 / 监控 / 慢查询排查 | 运维复杂度翻倍，需要 DBA 认可 |
| **跨库无法同事务** | 图内容与图集状态不能原子更新，直接影响 8.5 的待审回滚 |
| 权限判定跨数据源 | 每个图请求都要去 MySQL 查图集，不能 JOIN，缓存变成刚需（8.3） |
| 两套 SQL 方言 | 同一个 MyBatis-Plus 工程里要注意分页、逻辑删除、类型映射的差异 |

如果后续判断这些代价不值得（尤其是内容查询一直没用上），把图表搬回 MySQL 是低成本的——`JSONB` 换 `JSON`、生成列换 `JSON_LENGTH`、部分索引换普通索引，接口层完全不动。

### 2.3 存储粒度：图为单位，节点不建表

节点作为 `content.nodes[]` 随整张图一起存取，**不做节点级的表与接口**。理由：

1. 节点 schema 是开放的（`data.style` 为任意键值字典，见 `06-nodes.md` 第 2 节），关系表存不下，拆成一堆可空列等于没建表。
2. 一次编辑会话通常改动一张图里的多个节点与边，图粒度的全量写比节点粒度的增量写简单得多，也不需要跨行事务。
3. 前端现有模型（内存改动 → 防抖本地草稿 → 显式全量保存）本来就是图粒度的，不需要改造。

代价是保存时传输整张图。按每张图数百节点估算，序列化后在数百 KB 量级，可接受；超限保护见 8.4。

### 2.4 ID 策略：图 id 由前端生成的字符串

这是与图集服务**不一致但刻意为之**的一点，需要后端明确接受。

| 实体 | ID 类型 | 生成方 |
|---|---|---|
| 图集 `atlasId` | `BIGINT` 自增 | 服务端 |
| **图 `graphId`** | **`VARCHAR(32)`，形如 `gph_a1b2c3d4e5f6`** | **前端** |
| 节点 `nodeId` | `VARCHAR(32)`，形如 `nod_xxx` | 前端（仅存在于 JSON 内） |

理由是二者的**生命周期不同**：

- 图集永远由服务端创建，前端拿到返回值才有 id，没有离线态。
- 图在前端**先于服务端存在**。用户新建一张子图时，父图里会立刻多出一个指向它的子图入口节点（`data.subgraphId = 'gph_xxx'`），这一切发生在本地草稿里，**要等到用户显式点「保存」才推送后端**。如果 id 由服务端分配，前端就必须在建图的瞬间发一次请求、拿到 id 才能画节点——这会打破整个「本地草稿 + 显式保存」模型（见 `04-workplace-up.md` 第 4 节）。
- 图集导入时前端已经在做全量 id 重映射，同样依赖客户端生成能力。

后端对 `graphId` 只做两件事：**校验格式**（`^gph_[0-9a-f]{12}$`）与**校验唯一性**。不做业务解释。

### 2.5 图内容不解析

`content` 字段（`{ nodes, edges, viewport }`）对后端是**不透明文档**：

1. 后端只校验它是合法 JSON、顶层含 `nodes` / `edges` 数组、总体积不超限。
2. **不校验、不裁剪、不丢弃** `nodes[].data` 内部的任何字段，取回来的语义与存进去的完全一致。
3. 前端新增 style key、新增节点能力字段，都**不需要后端改代码，也不需要接口版本号**。

第 2 条是节点体系能长期演进的协议前提（`06-nodes.md` 7.2 节）。实现上把 DTO 的 `content` 声明为 `JsonNode` 或 `String`，不要映射成强类型对象——那样 Jackson 会把不认识的字段直接丢掉，正好破坏这条契约。

**一个 JSONB 特有的措辞修正**：`jsonb` 是解析后的二进制存储，会**规范化键顺序、去除重复键（保留最后一个）、丢弃空白**。所以契约保证的是**语义等价，不是字节级还原**——存进去 `{"b":1,"a":2}`，取出来可能是 `{"a":2,"b":1}`。

这对本项目没有任何影响：前端 `JSON.parse` 之后拿到的是对象，JSON 对象本来就无序，节点渲染不依赖键序。只有在有人拿原始文本做 diff 时才会看出区别。若将来真需要字节级保真，只能改用 `json` 类型（文本存储），代价是失去 GIN 索引能力——那等于放弃了选 PostgreSQL 的全部理由，不建议。

### 2.6 JSONB 落地注意事项

几个会在联调第一天撞上的点，提前写清楚：

**写入需要显式类型转换。** PostgreSQL JDBC 驱动默认把 `String` 参数当 `varchar` 发送，直接写 `jsonb` 列会报 `column "content" is of type jsonb but expression is of type character varying`。三种解法任选：

1. SQL 里显式转换：`... VALUES (..., CAST(#{content} AS jsonb))`（推荐，最显式）
2. 自定义 MyBatis `TypeHandler`，用 `PGobject` 并 `setType("jsonb")`
3. JDBC URL 加 `stringtype=unspecified`，让服务端推断类型（省事，但影响全库所有字符串参数，慎用）

**逻辑删除列用 `SMALLINT` 而非 `BOOLEAN`。** MyBatis-Plus `@TableLogic` 的默认值是 `0` / `1`，用 PostgreSQL 原生 `BOOLEAN` 需要额外配 `@TableLogic(value = "false", delval = "true")`，且 `SMALLINT` 与图集服务的列风格一致，省一处心智负担。

**分页与列名。** 本服务的列表接口不分页（7.1），暂时不涉及 MyBatis-Plus 的方言差异；但 `orderBy` 传列名时要注意 PostgreSQL 的标识符默认折叠为小写，与 MySQL 行为不同——建议后端维护一个列名白名单而不是直接拼接。

### 2.7 外部指向：后端无需感知

节点可以携带一个指向图集内其他节点的引用（`data.ref = { graphId, nodeId }`）。**本服务完全不需要知道它的存在**：

- 不校验目标是否存在（产品已确认放弃校验）。
- 不建引用索引表，不提供「谁指向我」「失效引用体检」之类的跨图查询。
- 删除节点或图时不做任何反向清理。

前端跳转时用 `graphs/detail` 拉目标图，在返回的 `nodes[]` 里找 `nodeId`，找不到就给用户报错。对后端而言，`ref` 只是 `content` 文档里的两个字符串，走 2.5 的透传契约即可。

选了 PostgreSQL 并不改变这一点——**本期不建 GIN 索引、不写任何查询 `content` 内部的 SQL**。它只是让「将来想加回来」这件事从「迁库」降级成「建一个索引」。

---

## 3. 概念模型：复用现有的图目录结构

前端已有一套成熟的目录抽象，**后端沿用同一套语义，不另造模型**。

### 3.1 语义

```
图集 Atlas（BIGINT）
 └── 图 Graph（gph_xxx）
      ├── parentGraphId = null  →  根图（目录树的顶层，对应工作区的一个 Tab）
      └── parentGraphId = 某图  →  内嵌图（该图的子目录）
```

- 目录关系由**图自身持有** `parentGraphId`，向上单向指，与前端 `Graph.parentGraphId` 完全一致。
- **路径不冗余存储**。`根图/子图A/子子图B` 这样的路径由 `parentGraphId` 链实时推导，改名和移动都不会失步。
- 一个图集可以有**多张根图**（`04-workplace-up.md` 第 1 节已确认，图集服务的「无根图概念」与此不冲突：图集不持有根图字段，根图由图侧的 `parentGraphId IS NULL` 判定）。

### 3.2 树在前端推导，后端只给平铺列表

后端**不提供**建树接口，`graphs/list` 返回图集内全部图的平铺元信息，前端用现有函数推导：

| 前端现有函数（`src/stores/workspaceStore.ts`） | 用途 |
|---|---|
| `buildGraphTree(graphs)` | 平铺列表 → 目录树（已带环防御） |
| `getGraphPath(graphs, id)` | 沿 `parentGraphId` 链推导面包屑路径 |
| `getDescendantIds(graphs, id)` | 取子树，用于关闭 Tab 与级联删除 |

这三个函数已经在跑，接后端时**不需要改一行**——只是数据来源从 localStorage 换成接口。这是「复用现有目录结构」最直接的体现：后端不需要理解树，只需要老实存好每张图的 `parent_graph_id`。

后端唯一需要理解树的地方是**两条完整性约束**（见 8.1、8.2）：父子必须同图集，以及不能成环。

### 3.3 与子图入口节点的关系

父图里指向子图的那个节点（`data.subgraphId`）和 `parentGraphId` 是**两条独立的边**：

| 关系 | 存在哪 | 谁维护 |
|---|---|---|
| 目录归属 | `graph.parentGraphId` | 后端字段 |
| 画布入口 | 父图 `content` 里某节点的 `data.subgraphId` | 前端，在 JSON 内 |

一张图**只能有一个目录位置**，但**可以被多处链接**（多个图里都放一个指向它的入口节点）。所以「新建子图」会写 `parentGraphId`，「链接已有图」不改动它。后端不需要维护两者的一致性，也无法维护——第二条边在不透明 JSON 里。

---

## 4. 数据模型

### 4.1 表结构

PostgreSQL 建表：

```sql
CREATE TABLE struct_mind_graph (
  graph_id        VARCHAR(32)  NOT NULL,
  atlas_id        BIGINT       NOT NULL,
  name            VARCHAR(64)  NOT NULL,
  parent_graph_id VARCHAR(32)  NULL,
  content         JSONB        NOT NULL,
  rev             INTEGER      NOT NULL DEFAULT 1,
  node_count      INTEGER      GENERATED ALWAYS AS
                                 (jsonb_array_length(COALESCE(content -> 'nodes', '[]'::jsonb))) STORED,
  deleted         SMALLINT     NOT NULL DEFAULT 0,
  created_at      BIGINT       NOT NULL,
  updated_at      BIGINT       NOT NULL,
  PRIMARY KEY (graph_id)
);

-- 部分索引：只索引未删除行，比 MySQL 的 (atlas_id, deleted) 复合索引更紧凑
CREATE INDEX idx_graph_atlas  ON struct_mind_graph (atlas_id)        WHERE deleted = 0;
CREATE INDEX idx_graph_parent ON struct_mind_graph (parent_graph_id) WHERE deleted = 0;

-- 本期不创建。将来若要恢复 ref 反查 / 服务端节点搜索，加这一条即可，无需改表：
-- CREATE INDEX idx_graph_content ON struct_mind_graph USING GIN (content jsonb_path_ops);

COMMENT ON COLUMN struct_mind_graph.graph_id        IS '前端生成，gph_ + 12 位十六进制';
COMMENT ON COLUMN struct_mind_graph.parent_graph_id IS '目录父节点，NULL 为根图';
COMMENT ON COLUMN struct_mind_graph.content         IS '{nodes,edges,viewport}，对后端不透明';
COMMENT ON COLUMN struct_mind_graph.rev             IS '内容版本号，乐观锁；rename/move 不递增';
```

设计说明：

- **`content` 单列 JSONB**，不拆 `nodes` / `edges` / `viewport` 三列。它们总是一起读写，拆开只增加事务面。超过约 2KB 时 PostgreSQL 会自动 TOAST 压缩并行外存储，几百 KB 的图实际落盘远小于原始 JSON，不需要应用层再压一次。
- **`node_count` 用生成列**而不是让前端上报，避免信任客户端。`jsonb_array_length` 与 `->` 都是 IMMUTABLE，可用于 `STORED` 生成列；外面套 `COALESCE` 是必须的——若 `content` 里没有 `nodes` 键，`jsonb_array_length(NULL)` 会让整行写入失败。
- **`parent_graph_id` 不加外键**。自引用外键在级联删除与批量导入时限制过多，完整性在服务层保证（8.1、8.2）。
- **部分索引**（`WHERE deleted = 0`）是 PostgreSQL 相对 MySQL 的一个顺手便宜：索引里不含已删除行，体积更小，且查询天然带这个条件。
- **`deleted` 用 `SMALLINT`** 而非 `BOOLEAN`，理由见 2.6。
- 时间戳用 `BIGINT` 毫秒，与图集服务的 `createdAt` / `updatedAt` 保持一致，跨库比对时不用做类型转换。

### 4.2 公共类型

**GraphMetaVO**（列表用，不含内容）

| 字段 | 类型 | 说明 |
|---|---|---|
| `graphId` | string | 图 ID |
| `atlasId` | number | 所属图集 |
| `name` | string | 图名 |
| `parentGraphId` | string \| null | 目录父节点，`null` 为根图 |
| `nodeCount` | number | 节点数，目录树与子图入口节点展示用 |
| `rev` | number | 当前内容版本 |
| `createdAt` | number | 毫秒 |
| `updatedAt` | number | 毫秒 |

**GraphVO** = `GraphMetaVO` +：

| 字段 | 类型 | 说明 |
|---|---|---|
| `content` | object | `{ nodes: [], edges: [], viewport?: {x,y,zoom} }`，原样返回 |

**GraphSaveResultVO**（批量保存的逐项结果）

| 字段 | 类型 | 说明 |
|---|---|---|
| `graphId` | string | |
| `ok` | boolean | 是否保存成功 |
| `rev` | number | 成功时为新版本号；失败时为服务端当前版本 |
| `code` | int | 失败原因，成功为 `0` |

---

## 5. 公共约定

### 5.1 响应信封

与图集服务完全一致：

```json
{ "code": 0, "message": "ok", "data": {} }
```

时间戳一律毫秒级 Unix 时间。

### 5.2 鉴权与权限

图**没有独立的权限模型，一切继承所属图集**。

| 操作 | 要求 | 判定方式 |
|---|---|---|
| 读（`list` / `detail`） | 图集对当前请求者可见 | 图集 `status=PUBLISHED` 且 `visible=1`，或 `authorId ∈ allUserIds` |
| 写（`save` / `rename` / `move` / `delete`） | `VERIFIED` + 图集可编辑 | `authorId ∈ allUserIds`（含委托），与图集服务的 `editable` 同一套逻辑 |

实现要点：

- 每个接口入口先由 `graphId` 或 `atlasId` 取到图集，复用图集服务已有的权限判定方法，**不要复制一份判定逻辑**。
- **图集在 MySQL、图在 PostgreSQL，这个判定是一次跨数据源查询，不能写成 JOIN。** 走图集服务的 Java 方法，不要在图的 SQL 里拼图集表。
- 批量接口（`batch-save`）的图必须属于同一图集，只判一次权限。
- 图集不存在 / 无权 时直接复用图集服务的 `52030`。

### 5.3 建议错误码

沿用图集服务的分段风格，需要在 `doc/14-错误码.md` 登记后使用：

| code | 含义 | HTTP | 典型场景 |
|---|---|---|---|
| `42110` | 图内容超出体积限制 | 400 | `content` 序列化后超过上限 |
| `42120` | 目录位置非法 | 400 | 父图跨图集 / 自引用 / 成环 |
| `42130` | 图已被修改，请刷新 | 409 | `rev` 不匹配 |
| `42140` | 图 ID 格式非法或已存在 | 400 | 首次保存时 `graphId` 冲突 |
| `52050` | 图不存在 / 无权操作 | 500 | 与图集服务 `52030` 同风格 |

图集相关的 `31010`（需登录）、`31020`（需实名）、`40000`（参数非法）、`52030`（图集不存在 / 无权）直接复用。

### 5.4 版本号 `rev` 只保护内容

`rev` 是**内容的乐观锁**，不是整行的版本：

- `save` 必须带 `rev`，比对通过后服务端 `rev + 1`。
- **`rename` 与 `move` 不校验也不递增 `rev`。**

这条约定是刻意的。改名和调整目录位置发生在目录树面板上，此时画布往往有未保存的改动；如果它们也动 `rev`，用户「在树上改个名」就会让紧接着的画布保存报冲突，属于纯粹的误伤。改名与移动只写元信息列，与内容互不干扰。

---

## 6. 接口一览

| # | 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|---|
| 6.1 | POST | `/api/v1/struct-mind/graphs/list` | 可见即可 | 图集内全部图的元信息（前端据此建目录树） |
| 6.2 | POST | `/api/v1/struct-mind/graphs/detail` | 可见即可 | 单张图的完整内容 |
| 6.3 | POST | `/api/v1/struct-mind/graphs/save` | 可编辑 | **Upsert**：全量保存内容，不存在则创建 |
| 6.4 | POST | `/api/v1/struct-mind/graphs/batch-save` | 可编辑 | 批量保存，对应「全部保存」 |
| 6.5 | POST | `/api/v1/struct-mind/graphs/rename` | 可编辑 | 改名 |
| 6.6 | POST | `/api/v1/struct-mind/graphs/move` | 可编辑 | 调整目录位置（改 `parentGraphId`） |
| 6.7 | POST | `/api/v1/struct-mind/graphs/delete` | 可编辑 | 删除图及其全部子孙 |
| 6.8 | POST | `/api/v1/struct-mind/graphs/list-content` | 可见即可 | 图集内全部图的完整内容（导出用，带体积保护） |

**没有独立的 create 接口**，这是刻意的设计，见 6.3。

---

## 7. 接口详情

### 7.1 图列表（目录树数据源）

- **路由**：`POST /api/v1/struct-mind/graphs/list`
- **鉴权**：图集可见即可（匿名可读已发布图集）
- **功能**：返回图集内全部图的元信息，前端用 `buildGraphTree` 推导目录树、渲染 Tab 与面包屑
- **逻辑**：校验图集可见 → `WHERE atlas_id = ? AND deleted = 0` → 按 `created_at` 升序返回全部（不分页）

**Request Body**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `atlasId` | number | 是 | 图集 ID |
| `keyword` | string | 否 | 按图名模糊过滤；用于目录树搜索 |

```json
{ "atlasId": 1 }
```

**Response `data`**：`GraphMetaVO[]`

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    { "graphId": "gph_a1b2c3d4e5f6", "atlasId": 1, "name": "总览",
      "parentGraphId": null, "nodeCount": 12, "rev": 7,
      "createdAt": 1754067780000, "updatedAt": 1754070000000 },
    { "graphId": "gph_1122334455aa", "atlasId": 1, "name": "鉴权流程",
      "parentGraphId": "gph_a1b2c3d4e5f6", "nodeCount": 25, "rev": 3,
      "createdAt": 1754067900000, "updatedAt": 1754069000000 }
  ]
}
```

**不分页**是有意的，有两个理由：

1. **目录树必须完整才能建**，缺一张图整棵子树就断了。
2. **外部指向的跳转依赖完整的 `parentGraphId` 链**。指向的目标可能是一张从未打开过的深层内嵌图，前端要沿链向上推出它所属的根图才能落位到正确的标签页（见 `06-nodes.md` 6.3）；链上缺一环，路径就断在那里，会把一张内嵌图误当成根图。

图集内图数量在产品上是有限的（数十到数百），元信息一行不到 200 字节，全量返回可接受。`keyword` 只用于目录树的前端过滤场景，**不要用它来缩减建树所需的数据**。

```bash
curl -X POST http://localhost:8080/api/v1/struct-mind/graphs/list \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"atlasId":1}'
```

---

### 7.2 图详情

- **路由**：`POST /api/v1/struct-mind/graphs/detail`
- **鉴权**：图集可见即可
- **功能**：拉取单张图的完整内容，供画布渲染
- **逻辑**：按 `graphId` 取图 → 反查图集判可见 → 原样返回 `content`

**Request Body**

```json
{ "graphId": "gph_a1b2c3d4e5f6" }
```

**Response `data`**：`GraphVO`

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "graphId": "gph_a1b2c3d4e5f6",
    "atlasId": 1,
    "name": "总览",
    "parentGraphId": null,
    "nodeCount": 2,
    "rev": 7,
    "createdAt": 1754067780000,
    "updatedAt": 1754070000000,
    "content": {
      "nodes": [
        {
          "id": "nod_a1b2c3d4e5f6",
          "type": "cell",
          "position": { "x": 240, "y": 120 },
          "width": 160,
          "height": 80,
          "data": {
            "label": "鉴权中心",
            "description": "",
            "style": { "shape": "roundRect", "fillColor": "surface-2" },
            "subgraphId": "gph_1122334455aa",
            "ref": { "graphId": "gph_66778899bbcc", "nodeId": "nod_ffeeddccbbaa", "labelSnapshot": "Token 校验" }
          }
        },
        {
          "id": "nod_9f8e7d6c5b4a",
          "type": "cell",
          "position": { "x": 480, "y": 120 },
          "width": 140,
          "height": 90,
          "data": { "label": "是否已登录", "style": { "shape": "diamond" } }
        }
      ],
      "edges": [
        { "id": "edg_001", "source": "nod_a1b2c3d4e5f6", "sourceHandle": "e",
          "target": "nod_9f8e7d6c5b4a", "targetHandle": "w" }
      ],
      "viewport": { "x": 0, "y": 0, "zoom": 1 }
    }
  }
}
```

`data.style`、`data.subgraphId`、`data.ref`、`sourceHandle` 等字段后端**一概不解析**，此处列出只是让后端理解载荷长什么样。

```bash
curl -X POST http://localhost:8080/api/v1/struct-mind/graphs/detail \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"graphId":"gph_a1b2c3d4e5f6"}'
```

---

### 7.3 保存图（Upsert，无独立 create）

- **路由**：`POST /api/v1/struct-mind/graphs/save`
- **鉴权**：`VERIFIED` + 图集可编辑
- **功能**：全量保存一张图的内容；图不存在时**创建**
- **逻辑**：
  1. 校验 `graphId` 格式与 `content` 体积
  2. 查图：
     - **不存在** → 要求 `rev` 缺省或为 `0`，插入新行（`rev = 1`）；校验 `parentGraphId` 合法（8.1、8.2）
     - **存在** → 要求 `rev` 与库中一致，否则 `42130`；更新 `content`、`rev + 1`、`updated_at`
  3. 返回新的 `GraphMetaVO`

**为什么没有独立的 create 接口**：前端的图**先在本地存在**——新建一张子图时，本地立刻生成 `gph_xxx`、在父图里画上入口节点、标记为脏，等用户点「保存」才推送后端。这个流程里根本没有「向服务端申请创建」这一步。把 create 折叠进 save，接口数量少一个，前端也少一条容易漏掉的分支（新建后未保存就关闭标签页，服务端不该留下一张空图）。

`rev` 的取值同时承担了「我期望创建还是更新」的语义，因此**并发首次保存是安全的**：两个客户端同时用 `rev=0` 保存同一个 `graphId`，只有一个能插入成功，另一个撞主键冲突返回 `42140`。

**Request Body**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `graphId` | string | 是 | `^gph_[0-9a-f]{12}$` |
| `atlasId` | number | 是 | 仅创建时使用；更新时校验与库中一致，不允许跨图集改归属 |
| `name` | string | 是 | ≤ 64 |
| `parentGraphId` | string \| null | 否 | 仅创建时生效；更新请走 `move` |
| `rev` | number | 是 | 创建传 `0`；更新传上次读到的值 |
| `content` | object | 是 | `{ nodes, edges, viewport? }` |

```json
{
  "graphId": "gph_1122334455aa",
  "atlasId": 1,
  "name": "鉴权流程",
  "parentGraphId": "gph_a1b2c3d4e5f6",
  "rev": 3,
  "content": { "nodes": [], "edges": [], "viewport": { "x": 0, "y": 0, "zoom": 1 } }
}
```

**Response `data`**：`GraphMetaVO`（含新的 `rev`，前端必须用它覆盖本地值）

**冲突响应**：

```json
{ "code": 42130, "message": "图已被修改，请刷新后重试", "data": { "rev": 9 } }
```

`data.rev` 带上服务端当前版本，前端据此提示「覆盖保存 / 放弃本地」——选覆盖就用这个 `rev` 重发一次。

```bash
curl -X POST http://localhost:8080/api/v1/struct-mind/graphs/save \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d @graph-save.json
```

---

### 7.4 批量保存

- **路由**：`POST /api/v1/struct-mind/graphs/batch-save`
- **鉴权**：`VERIFIED` + 图集可编辑
- **功能**：对应工作区操作菜单的「全部保存」与关闭标签时的「保存并关闭」
- **逻辑**：校验全部图同属一个图集 → **逐张独立处理**（每张一个事务）→ 汇总逐项结果

**关键语义：不是一个大事务，允许部分成功。** 一张图的版本冲突不应该阻止其他图保存——用户点「全部保存」的诉求是尽可能多地落盘，而不是要么全成要么全败。前端按逐项结果决定清除哪些脏标、对哪些图弹冲突提示。

**Request Body**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `atlasId` | number | 是 | 全部图必须属于该图集 |
| `graphs` | object[] | 是 | 每项结构同 `save` 的 Body（除 `atlasId`），单次上限 **20** 张 |

**Response `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `results` | GraphSaveResultVO[] | 与入参等长、同序 |
| `okCount` | number | 成功张数 |

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "okCount": 2,
    "results": [
      { "graphId": "gph_a1b2c3d4e5f6", "ok": true,  "rev": 8, "code": 0 },
      { "graphId": "gph_1122334455aa", "ok": true,  "rev": 4, "code": 0 },
      { "graphId": "gph_66778899bbcc", "ok": false, "rev": 9, "code": 42130 }
    ]
  }
}
```

顶层 `code` 仍为 `0`——请求本身是成功的，个别图的失败在 `results` 里表达。

---

### 7.5 图改名

- **路由**：`POST /api/v1/struct-mind/graphs/rename`
- **鉴权**：`VERIFIED` + 图集可编辑
- **功能**：修改图名，供目录树与 Tab 上的重命名使用
- **逻辑**：只更新 `name` 与 `updated_at`；**不校验也不递增 `rev`**（见 5.4）

**Request Body**

```json
{ "graphId": "gph_1122334455aa", "name": "鉴权流程（修订）" }
```

**Response `data`**：`GraphMetaVO`

图名在图集内**不要求唯一**——同名图在不同目录位置下是合理的，唯一性由目录路径表达。

---

### 7.6 移动图（调整目录位置）

- **路由**：`POST /api/v1/struct-mind/graphs/move`
- **鉴权**：`VERIFIED` + 图集可编辑
- **功能**：改变图在目录树中的位置，包括「降为某图的子图」与「提升为根图」
- **逻辑**：校验目标父图存在且同图集 → **环检测**（8.2）→ 更新 `parent_graph_id`；不动 `rev`

**Request Body**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `graphId` | string | 是 | 被移动的图 |
| `parentGraphId` | string \| null | 是 | 目标父图；传 `null` 提升为根图 |

```json
{ "graphId": "gph_1122334455aa", "parentGraphId": null }
```

**Response `data`**：`GraphMetaVO`

注意本接口只改**目录归属**，不会去父图的画布里增删子图入口节点——那条边在不透明的 `content` JSON 里，由前端维护（3.3 节）。

---

### 7.7 删除图（级联子孙）

- **路由**：`POST /api/v1/struct-mind/graphs/delete`
- **鉴权**：`VERIFIED` + 图集可编辑
- **功能**：删除一张图及其**全部子孙图**，对齐前端目录树的「删除即删整棵子树」语义
- **逻辑**：取图集内全部 `(graphId, parentGraphId)` → 内存里向下收集子树 → 批量逻辑删除 → 返回被删除的 id 列表

**为什么级联而不是把子图提升为根图**：目录树上删一个目录，用户的心理预期是整个目录消失。前端 `confirmDelete` 已经是这个行为（配合 `getDescendantIds`），后端保持一致，避免两侧语义分叉产生孤儿图。

**Request Body**

```json
{ "graphId": "gph_a1b2c3d4e5f6" }
```

**Response `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `deletedGraphIds` | string[] | 实际删除的全部 id（含子孙），前端据此清理本地状态 |

```json
{ "code": 0, "message": "ok", "data": { "deletedGraphIds": ["gph_a1b2c3d4e5f6", "gph_1122334455aa"] } }
```

**不清理引用**：其他图里指向被删除图的子图入口节点（`data.subgraphId`）与外部指向（`data.ref`）都保持原样，后端不去改别的图的 `content`。前端在渲染时发现目标不存在，显示为失效状态即可（`06-nodes.md` 5.3、6.4）。

---

### 7.8 图集内容全量拉取（导出用）

- **路由**：`POST /api/v1/struct-mind/graphs/list-content`
- **鉴权**：图集可见即可
- **功能**：一次性取回图集内全部图的完整内容，用于「导出整个图集」与小图集的整体加载
- **逻辑**：校验可见 → 汇总体积，超过上限（建议 **20MB**）返回 `42110` 并提示改用逐图拉取

**Request Body**

```json
{ "atlasId": 1 }
```

**Response `data`**：`GraphVO[]`

这个接口是为导出准备的，**不建议作为工作区的常规加载方式**。日常应当走「`list` 拿元信息建树 → 打开哪张图就 `detail` 哪张」的懒加载路径，避免一进工作区就拉几 MB。

---

## 8. 关键实现要点

### 8.1 父子必须同图集

`parentGraphId` 指向的图必须与自身 `atlasId` 相同。跨图集的父子关系会让目录树跨越图集边界，破坏「图集是关联关系的边界」这一产品前提。

在 `save`（创建分支）与 `move` 两处校验，不通过返回 `42120`。

### 8.2 环检测

`move` 必须防止 `A → B → A` 这类环，否则前端建树会死循环（`buildGraphTree` 虽有环防御，但那是兜底，不该依赖）。

推荐做法——**从目标父图向上走 `parentGraphId` 链**，若走到了被移动的图自身则成环：

```
move(graphId = X, parentGraphId = P):
  if P == null            -> 合法（提升为根图）
  if P == X               -> 非法（自引用）
  cur = P
  while cur != null:
      if cur == X         -> 非法（P 在 X 的子树内）
      cur = parentOf(cur)
  合法
```

图集内图数量有限，链长通常个位数，直接在内存里走即可；建议加一个最大迭代次数（如 100）防御脏数据里已有的环。

`save` 的创建分支同样要校验（新图的父不可能在其子树内，只需校验父存在且同图集）。

### 8.3 权限判定的开销（分库后更重要）

每个接口都要反查图集判权，而图集在**另一个数据库**里——这已经不是一次本地 JOIN，而是一次独立的跨库往返。缓存从「优化」变成「刚需」：

- `graphId → atlasId` 与图集权限信息做一层短 TTL 本地缓存（如 Caffeine，30 秒）。图集行很小且变更不频繁，命中率会很高。
- 批量接口一次性判权，不要在循环里逐图查——否则 20 张图就是 20 次跨库查询。
- 缓存失效窗口内可能出现「刚被取消编辑权限的用户还能写一次」。30 秒的窗口对本产品可接受；若不接受，写操作跳过缓存、只让读操作走缓存。

### 8.4 体积限制

| 项 | 建议上限 | 超限行为 |
|---|---|---|
| 单图 `content` 序列化后 | 2 MB | `42110` |
| `batch-save` 单次图数 | 20 | `40000` |
| `batch-save` 总体积 | 8 MB | `42110` |
| `list-content` 响应总体积 | 20 MB | `42110`，提示改用逐图拉取 |

同时需要调整容器与框架的请求体上限（Tomcat `max-http-post-size`、网关的 `client_max_body_size`），否则会在业务校验之前被拦掉，前端拿到的是一个没有信封的 413。

数据库侧不需要额外调参：PostgreSQL 的 `jsonb` 单值上限为 255MB，且超过约 2KB 会自动 TOAST 压缩行外存储，2MB 的图完全在舒适区。

### 8.5 图集状态联动（待产品确认）

`doc-plan/05-atlas-api.md` 1.4 节提到「待审期间任何改动 → 回滚为 draft」，但已实现的图集服务（`doc/21`）没有描述这条规则。

如果产品要保留该规则，**图内容的写接口（`save` / `batch-save` / `rename` / `move` / `delete`）都必须触发回滚**——图内容显然属于「图集的改动」。建议做法是在图服务的写入口统一调用图集服务提供的一个内部方法 `rollbackToDraftIfPending(atlasId)`，而不是在图服务里直接改图集表。

**分库让这条规则变得不干净**：图写 PostgreSQL、图集状态写 MySQL，两者**无法在同一个事务里完成**。存在这样的中间态——图已保存成功，但图集回滚 draft 失败（或反过来）。可选的兜底：

1. **先回滚图集、后写图**。图集回滚失败就直接返回错误、不写图；图写失败则图集已经是 draft，属于「多回滚了一次」的良性偏差（用户重新提交审核即可）。**推荐这个顺序**，因为它的失败模式最无害。
2. 引入本地消息表 / 最终一致，为这条规则单独上分布式事务——对一个「状态回滚」来说过重，不建议。

**这一条需要在开发前确认**：它既决定图服务是否要反向依赖图集服务的写能力，也是分库方案里唯一一处真正丢失原子性的地方。如果产品最终不要这条规则，分库的事务代价就等于零。

### 8.6 逻辑删除与 id 复用

图删除后 `graph_id` 行仍在（`deleted = 1`）。由于主键是 `graph_id`，**被删除的 id 不能被重新插入**。这在实践中不是问题（前端每次生成新 uuid），但要注意：`save` 的创建分支若撞到一条 `deleted = 1` 的同 id 行，应返回 `42140` 而不是静默复活它——复活会带来一张内容与目录位置都对不上的僵尸图。

---

## 9. 与前端对接对照

| 前端动作（`src/stores/workspaceStore.ts`） | 接口 |
|---|---|
| `loadAtlas` 加载图集 | `graphs/list`（元信息建树） |
| `openGraph` / `setActiveGraph` 首次打开某图 | `graphs/detail`（按需加载内容） |
| `createGraph` 新建图 | **无请求**，本地生成 `gph_xxx` 并标脏；首次 `graphs/save` 时落库 |
| `saveActiveGraph`「保存当前图」 | `graphs/save` |
| `saveAllDirty`「全部保存」 | `graphs/batch-save` |
| 目录树 / Tab 重命名 | `graphs/rename` |
| 目录树拖拽调整层级 | `graphs/move` |
| `confirmDelete` 删除图及子孙 | `graphs/delete` |
| 「导出整个图集」 | `graphs/list-content` |
| 点击外部指向角标跳转 | `graphs/detail`（目标图内容）；所属根图与目录路径由 `graphs/list` 已拉到的元信息推导，无额外请求 |
| `buildGraphTree` / `getGraphPath` / `getDescendantIds` | **无请求**，纯前端推导，接后端后不改 |

前端需要配套的改造：

1. `src/services/storage.ts` 的 `StorageProvider` 增加 `HttpProvider` 实现，`loadAtlas` 从「一次性加载全部图内容」改为「先拉元信息、按需拉内容」。
2. **`graphs` 里要能区分「仅元信息」与「已加载内容」两种条目**（加 `loaded` 标记或让 `nodes` 可缺省），否则未加载的图会被当成空图渲染。`buildGraphTree` / `getGraphPath` / `getDescendantIds` 只读元信息字段，不受影响。
3. `Graph` 类型增加 `rev` 字段，保存成功后用响应里的新 `rev` 覆盖本地值。
4. 新增版本冲突的处理 UI（覆盖保存 / 放弃本地）。
5. `openGraph` 增加「内容未加载则先拉取」的前置步骤——目录树点击与外部指向跳转都会走到它（`06-nodes.md` 6.3）。
6. 现有 `mockApi.saveGraphToServer` / `saveGraphsToServer` 的函数签名已经是图粒度与批量两种，替换实现即可，调用方不动。

---

## 10. 非目标（本文不覆盖）

- **节点 / 边级别的增量接口**：本期全量保存，协同编辑另议。
- **外部指向的校验与反向查询**：产品已确认放弃（`06-nodes.md` 6.4），本服务不感知 `ref`。
- **图集元信息、发布审核、标签**：见 `doc/21-图集服务-api.md`。
- **图的历史版本与回滚**：`rev` 只做乐观锁，不留历史快照。
- **服务端导入**：图集导入目前是纯前端行为（前端重映射 id 后逐图 `save`）；若后续要做服务端导入，在图集服务侧新增端点更合适。
- **自定义形状库的存储**：`06-nodes.md` 7.5 节规划在第三期，届时单独建表，与图服务无关。
