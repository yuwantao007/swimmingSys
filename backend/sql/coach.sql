-- 教练表
CREATE TABLE IF NOT EXISTS `coach` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '教练ID，主键',
    `name` VARCHAR(50) NOT NULL COMMENT '教练姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `specialty` VARCHAR(200) NOT NULL COMMENT '擅长项目（如：蛙泳、自由泳、蝶泳等）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '教练简介',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '授课状态：0-停用，1-在职',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_name` (`name`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练表';

-- 插入示例数据
INSERT INTO `coach` (`name`, `phone`, `gender`, `avatar`, `specialty`, `description`, `status`) VALUES
('张伟', '13800001001', 1, NULL, '自由泳、蝶泳', '国家一级游泳运动员，从事游泳教学10年，擅长自由泳和蝶泳技术指导。', 1),
('李娜', '13800001002', 2, NULL, '蛙泳、仰泳', '省级游泳冠军，专注于蛙泳和仰泳教学，耐心细致，深受学员喜爱。', 1),
('王强', '13800001003', 1, NULL, '自由泳、蛙泳、蝶泳', '拥有15年游泳教学经验，全能型教练，可教授多种泳姿。', 1),
('赵敏', '13800001004', 2, NULL, '儿童游泳启蒙', '专注儿童游泳教学8年，擅长游泳启蒙和恐水症克服，深受家长信赖。', 1),
('刘洋', '13800001005', 1, NULL, '竞技游泳、体能训练', '前省游泳队队员，擅长竞技游泳训练和体能提升指导。', 0);
