package com.swimmingsys.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程实体类
 */
@Data
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程类型（如：基础班、提高班、私教课）
     */
    private String courseType;

    /**
     * 教练ID，外键关联coach表
     */
    private Long coachId;

    /**
     * 课程开始时间
     */
    private LocalDateTime startTime;

    /**
     * 课程结束时间
     */
    private LocalDateTime endTime;

    /**
     * 课程容量（最大人数）
     */
    private Integer capacity;

    /**
     * 当前已预约人数
     */
    private Integer currentCount;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 课程状态：0-已下架，1-已发布
     */
    private Integer status;

    /**
     * 版本号（乐观锁）
     */
    @Version
    private Integer version;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
