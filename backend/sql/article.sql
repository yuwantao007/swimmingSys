-- 文章资料表
CREATE TABLE articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(255) NOT NULL COMMENT '资料标题',
    content TEXT COMMENT '资料内容',
    summary VARCHAR(500) COMMENT '资料摘要',
    cover_image VARCHAR(500) COMMENT '封面图片路径',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    author_id BIGINT NOT NULL COMMENT '作者ID（管理员ID）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-发布，2-隐藏',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    is_featured TINYINT DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) COMMENT='文章资料表';

-- 文章资料附件表
CREATE TABLE article_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '资料ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(50) COMMENT '文件类型（image/text）',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) COMMENT='文章资料附件表';

-- 文章资料分类表
CREATE TABLE article_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) COMMENT='文章资料分类表';