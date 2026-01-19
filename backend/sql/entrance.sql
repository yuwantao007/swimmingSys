-- 入场二维码表
CREATE TABLE IF NOT EXISTS `entrance_qrcode` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '二维码记录ID，主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，外键关联user表',
    `user_name` VARCHAR(50) NOT NULL COMMENT '用户姓名（冗余字段）',
    `qrcode_token` VARCHAR(64) NOT NULL COMMENT '二维码令牌（唯一）',
    `booking_id` BIGINT COMMENT '关联的预约ID（可选）',
    `course_name` VARCHAR(100) COMMENT '课程名称（冗余字段）',
    `course_start_time` DATETIME COMMENT '课程开始时间（冗余字段）',
    `coach_name` VARCHAR(50) COMMENT '教练姓名（冗余字段）',
    `generated_time` DATETIME NOT NULL COMMENT '生成时间',
    `is_used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用：0-未使用，1-已使用',
    `used_time` DATETIME COMMENT '使用时间',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_qrcode_token` (`qrcode_token`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_used` (`is_used`),
    INDEX `idx_generated_time` (`generated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入场二维码表';

-- 入场记录表
CREATE TABLE IF NOT EXISTS `entrance_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入场记录ID，主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，外键关联user表',
    `user_name` VARCHAR(50) NOT NULL COMMENT '用户姓名（冗余字段）',
    `qrcode_token` VARCHAR(64) NOT NULL COMMENT '二维码令牌',
    `entrance_time` DATETIME NOT NULL COMMENT '入场时间',
    `verifier_id` BIGINT NOT NULL COMMENT '验证人ID（管理员）',
    `verifier_name` VARCHAR(50) COMMENT '验证人姓名（冗余字段）',
    `booking_id` BIGINT COMMENT '关联的预约ID（可选）',
    `course_name` VARCHAR(100) COMMENT '课程名称（冗余字段）',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_qrcode_token` (`qrcode_token`),
    INDEX `idx_entrance_time` (`entrance_time`),
    INDEX `idx_verifier_id` (`verifier_id`),
    INDEX `idx_booking_id` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入场记录表';
