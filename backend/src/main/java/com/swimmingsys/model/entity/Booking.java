package com.swimmingsys.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约实体类
 */
@Data
@TableName("booking")
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预约ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，外键关联user表
     */
    private Long userId;

    /**
     * 课程ID，外键关联course表
     */
    private Long courseId;

    /**
     * 预约时间
     */
    private LocalDateTime bookingTime;

    /**
     * 预约状态：0-已取消，1-已预约，2-已完成
     */
    private Integer status;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

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
