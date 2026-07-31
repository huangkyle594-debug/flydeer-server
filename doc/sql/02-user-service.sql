-- User service schema
-- user_info.id: application-assigned, starts at 10000000 with random step (not AUTO_INCREMENT)
-- user_delegate.id: internal AUTO_INCREMENT from 1, not exposed via API

CREATE TABLE IF NOT EXISTS `user_info` (
    `id`           BIGINT       NOT NULL COMMENT 'application-assigned id',
    `channel`      VARCHAR(16)  NOT NULL COMMENT 'GITEE | GITHUB | PHONE',
    `channel_uid`  VARCHAR(128) NOT NULL COMMENT 'oauth uid or normalized phone',
    `phone`        VARCHAR(32)  NULL COMMENT 'bound phone; required for verified oauth',
    `verified`     TINYINT      NOT NULL DEFAULT 0 COMMENT '1=verified (real-name)',
    `name`         VARCHAR(64)  NOT NULL DEFAULT '',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=active 0=disabled',
    `created_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_info_channel_uid` (`channel`, `channel_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_delegate` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL COMMENT 'user id',
    `granted_user_id` BIGINT       NOT NULL COMMENT 'granted user id',
    `status`          VARCHAR(16)  NOT NULL COMMENT 'PENDING | ACCEPTED | REVOKED',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_granted` (`user_id`, `granted_user_id`),
    KEY `idx_granted_user_id` (`granted_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
