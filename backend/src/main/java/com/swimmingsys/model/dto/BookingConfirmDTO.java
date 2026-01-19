package com.swimmingsys.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 确认预约DTO
 * 用于确认预约时的数据传输（支持强制替换冲突预约）
 */
@Data
public class BookingConfirmDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 是否强制替换（有时间冲突时替换原预约）
     */
    private Boolean forceReplace = false;

    /**
     * 要替换的预约ID（强制替换时必填）
     */
    private Long replaceBookingId;
}
