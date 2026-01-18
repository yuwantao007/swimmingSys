package com.swimmingsys.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教练实体类
 */
@Data
@TableName("coach")
public class Coach implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教练ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 教练姓名
     */
    private String name;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 擅长项目（如：蛙泳、自由泳、蝶泳等）
     */
    private String specialty;

    /**
     * 教练简介
     */
    private String description;

    /**
     * 授课状态：0-停用，1-在职
     */
    private Integer status;

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
