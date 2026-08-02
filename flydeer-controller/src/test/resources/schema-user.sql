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
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `delegator_id` BIGINT       NOT NULL,
    `delegated_id` BIGINT       NOT NULL,
    `status`       VARCHAR(16)  NOT NULL,
    `created_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE (`delegator_id`, `delegated_id`)
);

CREATE TABLE IF NOT EXISTS `atlas` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `name`         VARCHAR(64)  NOT NULL,
    `description`  VARCHAR(500) NOT NULL DEFAULT '',
    `author_id`    BIGINT       NOT NULL,
    `author_name`  VARCHAR(64)  NOT NULL DEFAULT '',
    `status`       VARCHAR(16)  NOT NULL,
    `tags_json`    VARCHAR(2000) NOT NULL DEFAULT '[]',
    `created_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
