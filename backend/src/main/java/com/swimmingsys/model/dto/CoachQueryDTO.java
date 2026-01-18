package com.swimmingsys.model.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 教练查询DTO
 * 用于分页查询教练列表时的条件传输
 */
@Data
public class CoachQueryDTO implements Serializable {

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
     * 教练姓名（模糊查询）
     */
    private String name;

    /**
     * 联系电话（模糊查询）
     */
    private String phone;

    /**
     * 擅长项目（模糊查询）
     */
    private String specialty;

    /**
     * 授课状态：0-停用，1-在职
     */
    private Integer status;
}
