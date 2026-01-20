package com.swimmingsys.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章资料附件实体类
 */
@Data
@TableName("article_attachments")
public class ArticleAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资料ID
     */
    private Long articleId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型（image/text）
     */
    private String fileType;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField("is_delete")
    private Integer isDelete;
}