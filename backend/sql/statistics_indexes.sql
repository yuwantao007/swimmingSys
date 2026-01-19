-- ==========================================
-- 统计分析模块索引优化SQL脚本
-- ==========================================

-- 使用数据库
USE swimming_sys;

-- ==========================================
-- user表索引优化
-- ==========================================

-- 优化会员统计查询（按角色、删除标记、状态）
CREATE INDEX idx_role_delete_status ON user(role, is_delete, status);

-- 优化会员增长趋势查询（按角色、删除标记、创建时间）
CREATE INDEX idx_role_delete_time ON user(role, is_delete, created_time);

-- 优化性别分布统计查询（按角色、删除标记、性别）
CREATE INDEX idx_role_delete_gender ON user(role, is_delete, gender);

-- ==========================================
-- booking表索引优化
-- ==========================================

-- 优化课程预约统计查询（按课程ID、状态、删除标记）
CREATE INDEX idx_course_status_delete ON booking(course_id, status, is_delete);

-- 优化预约趋势统计查询（按预约时间、状态、删除标记）
CREATE INDEX idx_time_status_delete ON booking(booking_time, status, is_delete);

-- 优化用户预约统计查询（按用户ID、状态、删除标记）
CREATE INDEX idx_user_status_delete ON booking(user_id, status, is_delete);

-- ==========================================
-- entrance_record表索引优化
-- ==========================================

-- 优化入场趋势统计查询（按入场时间、删除标记）
CREATE INDEX idx_time_delete ON entrance_record(entrance_time, is_delete);

-- 优化用户入场频率统计查询（按用户ID、入场时间、删除标记）
CREATE INDEX idx_user_time_delete ON entrance_record(user_id, entrance_time, is_delete);

-- 优化课程关联入场统计查询（按预约ID、删除标记）
CREATE INDEX idx_booking_delete ON entrance_record(booking_id, is_delete);

-- ==========================================
-- course表索引优化
-- ==========================================

-- 优化课程类型分布统计查询（按课程类型、状态、删除标记）
CREATE INDEX idx_type_status_delete ON course(course_type, status, is_delete);

-- 优化教练工作量统计查询（按教练ID、删除标记）
CREATE INDEX idx_coach_delete ON course(coach_id, is_delete);

-- ==========================================
-- 说明
-- ==========================================
-- 
-- 这些索引的目的是优化统计分析模块的查询性能：
-- 
-- 1. 联合索引遵循最左前缀原则，确保查询条件能够命中索引
-- 2. 覆盖索引减少回表查询，提升查询效率
-- 3. 索引设计考虑了常见的统计查询场景
-- 4. 避免创建过多索引影响写入性能
-- 
-- 注意事项：
-- - 执行前请确保表中已有数据，或者先执行表初始化脚本
-- - 如果索引已存在，可能会报错，可以先DROP后再CREATE
-- - 大数据量时创建索引可能需要较长时间
-- - 建议在业务低峰期执行
