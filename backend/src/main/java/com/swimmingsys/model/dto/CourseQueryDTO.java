package com.swimmingsys.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程查询DTO
 * 用于分页查询课程列表时的条件传输
 */
@Data
public class CourseQueryDTO implements Serializable {

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
     * 课程名称（模糊查询）
     */
    private String courseName;

    /**
     * 课程类型（精确查询）
     */
    private String courseType;

    /**
     * 教练ID（精确查询）
     */
    private Long coachId;

    /**
     * 课程状态：0-已下架，1-已发布
     */
    private Integer status;

    /**
     * 开始时间范围-起
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTimeBegin;

    /**
     * 开始时间范围-止
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTimeEnd;

    /**
     * 是否只查询可预约的课程
     */
    private Boolean bookableOnly;
}
