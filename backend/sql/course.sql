-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID，主键',
    `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
    `course_type` VARCHAR(50) NOT NULL COMMENT '课程类型（如：基础班、提高班、私教课）',
    `coach_id` BIGINT NOT NULL COMMENT '教练ID，外键关联coach表',
    `start_time` DATETIME NOT NULL COMMENT '课程开始时间',
    `end_time` DATETIME NOT NULL COMMENT '课程结束时间',
    `capacity` INT NOT NULL COMMENT '课程容量（最大人数）',
    `current_count` INT NOT NULL DEFAULT 0 COMMENT '当前已预约人数',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '课程描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '课程状态：0-已下架，1-已发布',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_coach_id` (`coach_id`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_status` (`status`),
    INDEX `idx_course_type` (`course_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 预约表
CREATE TABLE IF NOT EXISTS `booking` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID，主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，外键关联user表',
    `course_id` BIGINT NOT NULL COMMENT '课程ID，外键关联course表',
    `booking_time` DATETIME NOT NULL COMMENT '预约时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '预约状态：0-已取消，1-已预约，2-已完成',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_course_id` (`course_id`),
    INDEX `idx_status` (`status`),
    UNIQUE INDEX `uk_user_course` (`user_id`, `course_id`, `status`) COMMENT '同一用户同一课程同一状态唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- 插入课程示例数据
INSERT INTO `course` (`course_name`, `course_type`, `coach_id`, `start_time`, `end_time`, `capacity`, `current_count`, `description`, `status`) VALUES
('游泳入门班', '基础班', 1, '2026-02-01 09:00:00', '2026-02-01 10:30:00', 20, 5, '适合零基础学员，学习基本的水性和蛙泳入门技巧', 1),
('蛙泳提高班', '提高班', 2, '2026-02-01 14:00:00', '2026-02-01 15:30:00', 15, 8, '针对已掌握蛙泳基础的学员，进一步提升泳姿和速度', 1),
('自由泳进阶班', '提高班', 1, '2026-02-02 09:00:00', '2026-02-02 10:30:00', 15, 3, '学习自由泳技巧，提升游泳速度和耐力', 1),
('儿童游泳启蒙班', '基础班', 4, '2026-02-02 14:00:00', '2026-02-02 15:00:00', 10, 10, '专为6-12岁儿童设计的游泳启蒙课程', 1),
('私教一对一', '私教课', 3, '2026-02-03 10:00:00', '2026-02-03 11:00:00', 1, 0, '一对一专业指导，根据学员需求定制训练计划', 1);
