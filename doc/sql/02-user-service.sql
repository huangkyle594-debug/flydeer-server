-- User service schema
-- user_info.id: application-assigned, starts at 10000000 with random step (not AUTO_INCREMENT)
-- user_delegate.id: internal AUTO_INCREMENT from 1, not exposed via API

CREATE TABLE IF NOT EXISTS `user_info` (
    `id`           BIGINT       NOT NULL COMMENT 'application-assigned id',
    `channel`      VARCHAR(16)  NOT NULL COMMENT 'GITEE | GITHUB | PHONE',
    `channel_uid`  VARCHAR(128) NOT NULL COMMENT 'oauth uid, or SHA-256(phone+salt) hex for PHONE',
    `phone`        VARCHAR(32)  NULL COMMENT 'masked phone e.g. 138****5678; never plaintext',
    `phone_hash`   VARCHAR(64)  NULL COMMENT 'SHA-256(phone+salt) hex; set when phone bound',
    `verified`     TINYINT      NOT NULL DEFAULT 0 COMMENT '1=verified (real-name)',
    `name`         VARCHAR(64)  NOT NULL DEFAULT '',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=active 0=disabled',
    `created_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_info_channel_uid` (`channel`, `channel_uid`),
    KEY `idx_user_info_phone_hash` (`phone_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_delegate` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `delegator_id` BIGINT       NOT NULL COMMENT 'delegator user id',
    `delegated_id` BIGINT       NOT NULL COMMENT 'delegated user id',
    `status`       VARCHAR(16)  NOT NULL COMMENT 'PENDING | ACCEPTED | REVOKED',
    `created_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_delegator_delegated` (`delegator_id`, `delegated_id`),
    KEY `idx_delegated_id` (`delegated_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
