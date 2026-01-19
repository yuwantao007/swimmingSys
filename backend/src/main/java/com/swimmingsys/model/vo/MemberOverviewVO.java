package com.swimmingsys.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员总览统计VO
 */
@Data
@ApiModel("会员总览统计")
public class MemberOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("会员总数")
    private Long totalMembers;

    @ApiModelProperty("启用会员数")
    private Long activeMemberCount;

    @ApiModelProperty("停用会员数")
    private Long inactiveMemberCount;

    @ApiModelProperty("启用率（%）")
    private Double activeRate;

    @ApiModelProperty("今日新增会员")
    private Long todayNewMembers;

    @ApiModelProperty("本周新增会员")
    private Long weekNewMembers;

    @ApiModelProperty("本月新增会员")
    private Long monthNewMembers;
}
