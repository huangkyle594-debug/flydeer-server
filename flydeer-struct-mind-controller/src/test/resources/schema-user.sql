CREATE TABLE IF NOT EXISTS `user_info` (
    `id`           BIGINT       NOT NULL,
    `channel`      VARCHAR(16)  NOT NULL,
    `channel_uid`  VARCHAR(128) NOT NULL,
    `phone`        VARCHAR(32)  NULL,
    `verified`     TINYINT      NOT NULL DEFAULT 0,
    `name`         VARCHAR(64)  NOT NULL DEFAULT '',
    `status`       TINYINT      NOT NULL DEFAULT 1,
    `created_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE (`channel`, `channel_uid`)
);

CREATE TABLE IF NOT EXISTS `user_delegate` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL,
    `granted_user_id` BIGINT       NOT NULL,
    `status`          VARCHAR(16)  NOT NULL,
    `created_at`      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE (`user_id`, `granted_user_id`)
);
