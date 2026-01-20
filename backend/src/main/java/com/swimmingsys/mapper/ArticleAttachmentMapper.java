package com.swimmingsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swimmingsys.model.entity.ArticleAttachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章资料附件Mapper接口
 */
@Mapper
public interface ArticleAttachmentMapper extends BaseMapper<ArticleAttachment> {
}