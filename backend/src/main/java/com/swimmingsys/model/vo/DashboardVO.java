package com.swimmingsys.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 运营情况概览VO
 */
@Data
@ApiModel("运营情况概览")
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("会员总数")
    private Long memberCount;

    @ApiModelProperty("今日新增会员")
    private Long todayNewMembers;

    @ApiModelProperty("今日新增会员增长率")
    private String todayNewMembersGrowth;

    @ApiModelProperty("本月预约数")
    private Long monthBookings;

    @ApiModelProperty("本月预约数增长率")
    private String monthBookingsGrowth;

    @ApiModelProperty("今日入场人次")
    private Long todayEntrances;

    @ApiModelProperty("今日入场人次增长率")
    private String todayEntrancesGrowth;

    @ApiModelProperty("课程总数")
    private Long courseCount;

    @ApiModelProperty("教练总数")
    private Long coachCount;
}
