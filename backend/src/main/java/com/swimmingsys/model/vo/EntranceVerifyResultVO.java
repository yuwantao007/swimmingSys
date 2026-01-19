package com.swimmingsys.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 扫码验证结果VO
 */
@Data
public class EntranceVerifyResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会员姓名
     */
    private String userName;

    /**
     * 入场时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime entranceTime;

    /**
     * 课程信息（可选）
     */
    private CourseInfo courseInfo;

    /**
     * 课程信息内嵌类
     */
    @Data
    public static class CourseInfo implements Serializable {
        private static final long serialVersionUID = 1L;

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

        /**
         * 提示信息
         */
        private String tip;
    }
}
