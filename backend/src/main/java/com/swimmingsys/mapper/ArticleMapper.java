package com.swimmingsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swimmingsys.model.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章资料Mapper接口
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}