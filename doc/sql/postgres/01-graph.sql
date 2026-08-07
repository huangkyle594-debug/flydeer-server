-- Graph service (PostgreSQL 16 + JSONB)
-- Mounted only into the postgres container (not MySQL init).

CREATE TABLE IF NOT EXISTS struct_mind_graph (
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

CREATE INDEX IF NOT EXISTS idx_graph_atlas
  ON struct_mind_graph (atlas_id) WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_graph_parent
  ON struct_mind_graph (parent_graph_id) WHERE deleted = 0;

COMMENT ON COLUMN struct_mind_graph.graph_id        IS '前端生成，gph_ + 12 位十六进制';
COMMENT ON COLUMN struct_mind_graph.parent_graph_id IS '目录父节点，NULL 为根图';
COMMENT ON COLUMN struct_mind_graph.content         IS '{nodes,edges,viewport}，对后端不透明';
COMMENT ON COLUMN struct_mind_graph.rev             IS '内容版本号，乐观锁；rename/move 不递增';
