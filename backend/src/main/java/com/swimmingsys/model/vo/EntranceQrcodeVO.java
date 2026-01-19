package com.swimmingsys.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入场二维码VO
 */
@Data
public class EntranceQrcodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 二维码令牌
     */
    private String qrcodeToken;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime generatedTime;

    /**
     * 课程预约信息（可选）
     */
    private CourseBookingInfo courseBooking;

    /**
     * 课程预约信息内嵌类
     */
    @Data
    public static class CourseBookingInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 预约ID
         */
        private Long bookingId;

        /**
         * 课程名称
         */
        private String courseName;

        /**
         * 课程开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime startTime;

        /**
         * 教练姓名
         */
        private String coachName;
    }
}
