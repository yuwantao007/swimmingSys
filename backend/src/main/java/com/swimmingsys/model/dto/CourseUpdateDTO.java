package com.swimmingsys.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 更新课程DTO
 * 用于修改课程信息时的数据传输
 */
@Data
public class CourseUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程名称
     */
    @Size(max = 100, message = "课程名称不能超过100个字符")
    private String courseName;

    /**
     * 课程类型
     */
    @Size(max = 50, message = "课程类型不能超过50个字符")
    private String courseType;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 课程开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 课程结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 课程容量（最大人数）
     */
    @Min(value = 1, message = "课程容量至少为1人")
    private Integer capacity;

    /**
     * 课程描述
     */
    @Size(max = 500, message = "课程描述不能超过500个字符")
    private String description;

    /**
     * 课程状态：0-已下架，1-已发布
     */
    @Min(value = 0, message = "课程状态无效")
    @Max(value = 1, message = "课程状态无效")
    private Integer status;
}
