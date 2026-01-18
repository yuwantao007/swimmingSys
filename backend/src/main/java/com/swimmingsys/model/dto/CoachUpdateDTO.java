package com.swimmingsys.model.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 更新教练DTO
 * 用于修改教练信息时的数据传输
 */
@Data
public class CoachUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教练姓名
     */
    @Size(max = 50, message = "教练姓名不能超过50个字符")
    private String name;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phone;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Min(value = 0, message = "性别值无效")
    @Max(value = 2, message = "性别值无效")
    private Integer gender;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 擅长项目
     */
    @Size(max = 200, message = "擅长项目不能超过200个字符")
    private String specialty;

    /**
     * 教练简介
     */
    @Size(max = 500, message = "教练简介不能超过500个字符")
    private String description;

    /**
     * 授课状态：0-停用，1-在职
     */
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
