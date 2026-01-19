package com.swimmingsys.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 热门课程VO
 */
@Data
@ApiModel("热门课程")
public class HotCourseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("课程ID")
    private Long courseId;

    @ApiModelProperty("课程名称")
    private String courseName;

    @ApiModelProperty("课程类型")
    private String courseType;

    @ApiModelProperty("预约次数")
    private Long bookingCount;

    @ApiModelProperty("课程容量")
    private Integer capacity;

    @ApiModelProperty("当前人数")
    private Integer currentCount;

    @ApiModelProperty("上座率（%）")
    private Double occupancyRate;
}
