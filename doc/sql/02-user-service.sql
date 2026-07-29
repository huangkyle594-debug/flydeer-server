-- User service schema (draft — adjust before applying)
-- user.id: application-assigned, starts at 10000000 with random step (not AUTO_INCREMENT)
-- user_delegate.id: internal AUTO_INCREMENT from 1, not exposed via API

CREATE TABLE IF NOT EXISTS `user` (
    `id`           BIGINT       NOT NULL COMMENT 'application-assigned id',
    `channel`      VARCHAR(16)  NOT NULL COMMENT 'GITEE | GITHUB | PHONE',
    `channel_uid`  VARCHAR(128) NOT NULL COMMENT 'oauth uid or normalized phone',
    `phone`        VARCHAR(32)  NULL COMMENT 'bound phone; required for verified oauth',
    `verified`     TINYINT      NOT NULL DEFAULT 0 COMMENT '1=verified (real-name)',
    `nickname`     VARCHAR(64)  NOT NULL DEFAULT '',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=active 0=disabled',
    `created_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_channel_uid` (`channel`, `channel_uid`),
    UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_delegate` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `grantor_id`    BIGINT       NOT NULL COMMENT 'user who grants',
    `grantee_id`    BIGINT       NOT NULL COMMENT 'user who receives grant',
    `request_type`  VARCHAR(16)  NOT NULL COMMENT 'GRANT | RECEIVE',
    `status`        VARCHAR(16)  NOT NULL COMMENT 'PENDING | ACCEPTED | REJECTED | CANCELLED',
    `created_at`    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `responded_at`  DATETIME(3)  NULL,
    PRIMARY KEY (`id`),
    KEY `idx_delegate_grantor` (`grantor_id`),
    KEY `idx_delegate_grantee` (`grantee_id`),
    KEY `idx_delegate_pair_status` (`grantor_id`, `grantee_id`, `status`),
    CONSTRAINT `fk_delegate_grantor` FOREIGN KEY (`grantor_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_delegate_grantee` FOREIGN KEY (`grantee_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
