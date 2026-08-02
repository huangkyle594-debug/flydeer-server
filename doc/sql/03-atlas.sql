-- Atlas (struct-mind) schema
-- Graph rows are out of scope for this phase; atlas is independent.

CREATE TABLE IF NOT EXISTS `atlas` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `name`         VARCHAR(64)  NOT NULL,
    `description`  VARCHAR(500) NOT NULL DEFAULT '',
    `author_id`    BIGINT       NOT NULL,
    `author_name`  VARCHAR(64)  NOT NULL DEFAULT '',
    `status`       VARCHAR(16)  NOT NULL COMMENT 'draft | pending | published',
    `tags_json`    JSON         NOT NULL,
    `created_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_atlas_author_status` (`author_id`, `status`),
    KEY `idx_atlas_status_updated` (`status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
