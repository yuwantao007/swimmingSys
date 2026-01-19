package com.swimmingsys.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 入场时段分布VO
 */
@Data
@ApiModel("入场时段分布")
public class HourlyDistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("小时（0-23）")
    private Integer hour;

    @ApiModelProperty("入场次数")
    private Long entranceCount;
}
