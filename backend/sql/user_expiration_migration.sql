-- ============================================
-- 用户过期时间管理功能 - 数据库迁移脚本
-- ============================================

USE swimming_sys;

-- 1. 添加过期时间字段
ALTER TABLE `user` 
ADD COLUMN `expiration_time` DATETIME DEFAULT NULL COMMENT '账户过期时间，NULL表示永久有效' 
AFTER `status`;

-- 2. 创建索引优化查询性能
-- 单列索引：用于快速查询即将过期和已过期用户
CREATE INDEX `idx_expiration_time` ON `user`(`expiration_time`);

-- 联合索引：用于多条件组合查询（如查询启用的会员中即将过期的）
CREATE INDEX `idx_role_status_expiration` ON `user`(`role`, `status`, `expiration_time`);

-- 3. 数据初始化（可选）
-- 为现有会员设置默认1年有效期作为示例
-- UPDATE `user` 
-- SET `expiration_time` = DATE_ADD(NOW(), INTERVAL 1 YEAR) 
-- WHERE `role` = 1 AND `expiration_time` IS NULL;

-- 4. 验证数据
SELECT 
    COUNT(*) AS total_users,
    SUM(CASE WHEN expiration_time IS NULL THEN 1 ELSE 0 END) AS permanent_users,
    SUM(CASE WHEN expiration_time IS NOT NULL THEN 1 ELSE 0 END) AS expiring_users,
    SUM(CASE WHEN expiration_time < NOW() THEN 1 ELSE 0 END) AS expired_users
FROM `user`;

-- ============================================
-- 回滚脚本（如需回滚，请执行以下SQL）
-- ============================================
-- DROP INDEX `idx_expiration_time` ON `user`;
-- DROP INDEX `idx_role_status_expiration` ON `user`;
-- ALTER TABLE `user` DROP COLUMN `expiration_time`;
