package com.swimmingsys.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 趋势数据VO（用于会员增长、预约趋势、入场趋势等）
 */
@Data
@ApiModel("趋势数据")
public class TrendDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("时间标识（日期/周/月）")
    private String period;

    @ApiModelProperty("数值")
    private Long count;
}
