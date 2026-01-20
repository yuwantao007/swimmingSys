package com.swimmingsys.model.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 文章资料更新DTO
 */
@Data
public class ArticleUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资料标题
     */
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