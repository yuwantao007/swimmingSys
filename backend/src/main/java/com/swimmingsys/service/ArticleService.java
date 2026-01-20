package com.swimmingsys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swimmingsys.model.dto.ArticleAddDTO;
import com.swimmingsys.model.dto.ArticleQueryDTO;
import com.swimmingsys.model.dto.ArticleUpdateDTO;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.ArticleVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 文章资料服务接口
 */
public interface ArticleService {

    /**
     * 创建文章资料
     *
     * @param addDTO 添加信息
     * @param loginUser 当前登录用户
     * @return 创建的文章资料
     */
    ArticleVO createArticle(ArticleAddDTO addDTO, User loginUser);

    /**
     * 根据ID获取文章资料
     *
     * @param id 文章资料ID
     * @return 文章资料信息
     */
    ArticleVO getArticleById(Long id);

    /**
     * 获取文章资料列表（分页）
     *
     * @param queryDTO 查询条件
     * @return 文章资料分页列表
     */
    Page<ArticleVO> getArticleList(ArticleQueryDTO queryDTO);

    /**
     * 更新文章资料
     *
     * @param id 文章资料ID
     * @param updateDTO 更新信息
     * @param loginUser 当前登录用户
     * @return 更新后的文章资料
     */
    ArticleVO updateArticle(Long id, ArticleUpdateDTO updateDTO, User loginUser);

    /**
     * 删除文章资料
     *
     * @param id 文章资料ID
     * @param loginUser 当前登录用户
     * @return 是否删除成功
     */
    boolean deleteArticle(Long id, User loginUser);

    /**
     * 发布文章资料
     *
     * @param id 文章资料ID
     * @param loginUser 当前登录用户
     * @return 是否发布成功
     */
    boolean publishArticle(Long id, User loginUser);

    /**
     * 下架文章资料
     *
     * @param id 文章资料ID
     * @param loginUser 当前登录用户
     * @return 是否下架成功
     */
    boolean unpublishArticle(Long id, User loginUser);

    /**
     * 获取公开文章资料列表（分页）
     *
     * @param queryDTO 查询条件
     * @return 公开文章资料分页列表
     */
    Page<ArticleVO> getPublicArticleList(ArticleQueryDTO queryDTO);

    /**
     * 增加文章资料浏览次数
     *
     * @param id 文章资料ID
     * @return 是否增加成功
     */
    boolean increaseViewCount(Long id);

    /**
     * 点赞/取消点赞文章资料
     *
     * @param id 文章资料ID
     * @param loginUser 当前登录用户
     * @return 点赞状态（true为已点赞，false为未点赞）
     */
    boolean toggleLike(Long id, User loginUser);
}