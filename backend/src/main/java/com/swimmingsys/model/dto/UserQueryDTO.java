package com.swimmingsys.model.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 用户查询DTO
 * 用于分页查询用户列表时的条件传输
 */
@Data
public class UserQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认1
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    /**
     * 每页大小，默认10
     */
    @Min(value = 1, message = "每页大小不能小于1")
    private Integer pageSize = 10;

    /**
     * 用户账号（模糊查询）
     */
    private String userAccount;

    /**
     * 用户名（模糊查询）
     */
    private String userName;

    /**
     * 手机号码（模糊查询）
     */
    private String phone;

    /**
     * 邮箱地址（模糊查询）
     */
    private String email;

    /**
     * 角色：0-管理员，1-会员，2-非会员
     */
    private Integer role;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
