package com.swimmingsys.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 入场总览统计VO
 */
@Data
@ApiModel("入场总览统计")
public class EntranceOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("总入场次数")
    private Long totalEntrances;

    @ApiModelProperty("今日入场次数")
    private Long todayEntrances;

    @ApiModelProperty("本周入场次数")
    private Long weekEntrances;

    @ApiModelProperty("本月入场次数")
    private Long monthEntrances;
}
