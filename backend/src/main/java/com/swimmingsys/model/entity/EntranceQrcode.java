package com.swimmingsys.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入场二维码实体类
 */
@Data
@TableName("entrance_qrcode")
public class EntranceQrcode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 二维码记录ID，主键
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
     * 二维码令牌（唯一）
     */
    private String qrcodeToken;

    /**
     * 关联的预约ID（可选）
     */
    private Long bookingId;

    /**
     * 课程名称（冗余字段）
     */
    private String courseName;

    /**
     * 课程开始时间（冗余字段）
     */
    private LocalDateTime courseStartTime;

    /**
     * 教练姓名（冗余字段）
     */
    private String coachName;

    /**
     * 生成时间
     */
    private LocalDateTime generatedTime;

    /**
     * 是否已使用：0-未使用，1-已使用
     */
    private Integer isUsed;

    /**
     * 使用时间
     */
    private LocalDateTime usedTime;

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
