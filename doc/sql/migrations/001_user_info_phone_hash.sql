-- user_info 增加 phone_hash（已有库升级；新库由 02-user-service.sql 初始化）
-- 幂等：列/索引已存在则跳过

SET @db := DATABASE();

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_info' AND COLUMN_NAME = 'phone_hash'
);
SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE user_info ADD COLUMN phone_hash VARCHAR(64) NULL COMMENT ''SHA-256(phone+salt) hex; set when phone bound'' AFTER phone',
  'SELECT ''phone_hash column already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_info' AND INDEX_NAME = 'idx_user_info_phone_hash'
);
SET @sql := IF(
  @idx_exists = 0,
  'ALTER TABLE user_info ADD KEY idx_user_info_phone_hash (phone_hash)',
  'SELECT ''idx_user_info_phone_hash already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- PHONE 渠道：channel_uid 已是 hash 时回填 phone_hash
UPDATE user_info
SET phone_hash = channel_uid
WHERE channel = 'PHONE'
  AND phone_hash IS NULL
  AND channel_uid IS NOT NULL;
