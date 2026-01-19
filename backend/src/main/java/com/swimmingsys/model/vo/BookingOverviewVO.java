package com.swimmingsys.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 预约总览统计VO
 */
@Data
@ApiModel("预约总览统计")
public class BookingOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("总预约次数")
    private Long totalBookings;

    @ApiModelProperty("已预约数（当前有效）")
    private Long activeBookings;

    @ApiModelProperty("已完成预约数")
    private Long completedBookings;

    @ApiModelProperty("已取消预约数")
    private Long cancelledBookings;

    @ApiModelProperty("预约完成率（%）")
    private Double completionRate;

    @ApiModelProperty("预约取消率（%）")
    private Double cancellationRate;
}
