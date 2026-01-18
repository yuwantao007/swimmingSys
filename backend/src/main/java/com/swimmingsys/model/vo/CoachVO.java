package com.swimmingsys.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教练信息视图对象VO
 */
@Data
public class CoachVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教练ID
     */
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
     * 擅长项目
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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}
