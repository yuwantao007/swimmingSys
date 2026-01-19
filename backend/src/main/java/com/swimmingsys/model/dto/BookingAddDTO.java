package com.swimmingsys.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 预约课程DTO
 * 用于会员预约课程时的数据传输
 */
@Data
public class BookingAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
}
