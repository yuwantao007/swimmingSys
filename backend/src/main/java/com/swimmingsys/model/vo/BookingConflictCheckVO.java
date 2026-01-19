package com.swimmingsys.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 预约冲突检测结果VO
 * 用于返回时间冲突检测结果
 */
@Data
public class BookingConflictCheckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否存在时间冲突
     */
    private Boolean hasConflict;

    /**
     * 冲突的预约信息
     */
    private BookingVO conflictBooking;

    /**
     * 请求预约的课程信息
     */
    private CourseVO requestedCourse;
}
