package com.swimmingsys.model.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户账号（4-20位字母、数字、下划线）
     */
    @NotBlank(message = "用户账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户账号格式不正确，只能包含4-20位字母、数字、下划线")
    private String userAccount;

    /**
     * 密码（至少6位字符）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32位之间")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 用户名（2-50位字符）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50位之间")
    private String userName;

    /**
     * 手机号码（11位数字）
     */
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phone;

    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱地址格式不正确")
    private String email;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别值无效")
    @Max(value = 2, message = "性别值无效")
    private Integer gender;
}
