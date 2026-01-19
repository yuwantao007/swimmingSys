package com.swimmingsys.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员分布统计VO
 */
@Data
@ApiModel("会员分布统计")
public class MemberDistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("角色分布")
    private RoleDistribution roleDistribution;

    @ApiModelProperty("性别分布（仅男性和女性）")
    private GenderDistribution genderDistribution;

    /**
     * 角色分布
     */
    @Data
    @ApiModel("角色分布")
    public static class RoleDistribution implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty("管理员数量")
        private Long adminCount;

        @ApiModelProperty("会员数量")
        private Long memberCount;

        @ApiModelProperty("非会员数量")
        private Long nonMemberCount;
    }

    /**
     * 性别分布（不包含未知性别）
     */
    @Data
    @ApiModel("性别分布")
    public static class GenderDistribution implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty("男性数量")
        private Long maleCount;

        @ApiModelProperty("女性数量")
        private Long femaleCount;

        @ApiModelProperty("男性占比（%）")
        private Double maleRate;

        @ApiModelProperty("女性占比（%）")
        private Double femaleRate;
    }
}
