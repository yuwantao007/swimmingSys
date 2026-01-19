package com.swimmingsys.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入场记录实体类
 */
@Data
@TableName("entrance_record")
public class EntranceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 入场记录ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，外键关联user表
     */
    private Long userId;

    /**
     * 用户姓名（冗余字段）
     */
    private String userName;

    /**
     * 二维码令牌
     */
    private String qrcodeToken;

    /**
     * 入场时间
     */
    private LocalDateTime entranceTime;

    /**
     * 验证人ID（管理员）
     */
    private Long verifierId;

    /**
     * 验证人姓名（冗余字段）
     */
    private String verifierName;

    /**
     * 关联的预约ID（可选）
     */
    private Long bookingId;

    /**
     * 课程名称（冗余字段）
     */
    private String courseName;

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
