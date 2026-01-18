-- ============================================
-- 游泳馆会员管理系统 - 用户表
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS swimming_sys DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE swimming_sys;

-- ============================================
-- 用户表 (user)
-- 用于存储系统所有用户信息，包括管理员、会员、非会员
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
    `user_account` VARCHAR(50) NOT NULL COMMENT '用户账号，用于登录',
    `password` VARCHAR(64) NOT NULL COMMENT '密码，MD5加密存储',
    `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT NOT NULL DEFAULT 2 COMMENT '角色：0-管理员，1-会员，2-非会员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_account` (`user_account`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_role` (`role`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================
-- 模拟数据（5条）
-- 密码均为 123456 的MD5加密值：e10adc3949ba59abbe56e057f20f883e
-- ============================================
INSERT INTO `user` (`user_account`, `password`, `user_name`, `phone`, `email`, `gender`, `role`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800000001', 'admin@swimming.com', 1, 0, 1),
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13800000002', 'zhangsan@qq.com', 1, 1, 1),
('lisi', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13800000003', 'lisi@qq.com', 2, 1, 1),
('wangwu', 'e10adc3949ba59abbe56e057f20f883e', '王五', '13800000004', 'wangwu@qq.com', 1, 2, 1),
('zhaoliu', 'e10adc3949ba59abbe56e057f20f883e', '赵六', '13800000005', 'zhaoliu@qq.com', 2, 2, 1);
