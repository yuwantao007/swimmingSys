package com.swimmingsys.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量设置用户过期时间DTO
 */
@Data
public class UserBatchExpirationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 过期时间，NULL表示清空过期时间（永久有效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expirationTime;

    /**
     * 操作类型：set-设置固定时间，extend-延长天数，shorten-缩短天数，clear-清空过期时间
     */
    private String operationType;

    /**
     * 天数（用于extend和shorten操作）
     */
    private Integer days;
}
