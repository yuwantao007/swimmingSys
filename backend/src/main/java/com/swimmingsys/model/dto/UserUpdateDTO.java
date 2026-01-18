package com.swimmingsys.model.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 用户信息更新DTO
 * 用于修改用户信息时的数据传输
 * 管理员可修改全部字段，普通用户只能修改部分字段（userName、phone、email、gender、avatar）
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phone;

    /**
     * 邮箱地址
     */
    @Email(message = "邮箱地址格式不正确")
    private String email;

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
     * 角色：0-管理员，1-会员，2-非会员
     * 仅管理员可修改
     */
    @Min(value = 0, message = "角色值无效")
    @Max(value = 2, message = "角色值无效")
    private Integer role;

    /**
     * 状态：0-禁用，1-启用
     * 仅管理员可修改
     */
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;

    /**
     * 新密码
     * 仅管理员可修改
     */
    @Size(min = 6, max = 32, message = "密码长度必须在6-32位之间")
    private String password;
}
