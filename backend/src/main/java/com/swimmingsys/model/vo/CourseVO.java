package com.swimmingsys.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程信息视图对象VO
 */
@Data
public class CourseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    private Long id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程类型
     */
    private String courseType;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 教练姓名
     */
    private String coachName;

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
    private Integer capacity;

    /**
     * 当前已预约人数
     */
    private Integer currentCount;

    /**
     * 剩余名额
     */
    private Integer remainingCount;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 课程状态：0-已下架，1-已发布
     */
    private Integer status;

    /**
     * 是否可预约（状态为发布、有剩余名额、未开始）
     */
    private Boolean bookable;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}
