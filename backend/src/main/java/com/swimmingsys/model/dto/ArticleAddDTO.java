package com.swimmingsys.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章资料添加DTO
 */
@Data
public class ArticleAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资料标题
     */
    @NotBlank(message = "资料标题不能为空")
    private String title;

    /**
     * 资料内容
     */
    private String content;

    /**
     * 资料摘要
     */
    private String summary;

    /**
     * 封面图片路径
     */
    private String coverImage;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态：0-草稿，1-发布，2-隐藏
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 是否置顶：false-否，true-是
     */
    private Boolean isTop;

    /**
     * 是否推荐：false-否，true-是
     */
    private Boolean isFeatured;
}