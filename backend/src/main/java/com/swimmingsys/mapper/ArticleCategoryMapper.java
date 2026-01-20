package com.swimmingsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swimmingsys.model.entity.ArticleCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章资料分类Mapper接口
 */
@Mapper
public interface ArticleCategoryMapper extends BaseMapper<ArticleCategory> {
}