package com.swimmingsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.mapper.ArticleAttachmentMapper;
import com.swimmingsys.mapper.ArticleCategoryMapper;
import com.swimmingsys.mapper.ArticleMapper;
import com.swimmingsys.mapper.UserMapper;
import com.swimmingsys.model.dto.ArticleAddDTO;
import com.swimmingsys.model.dto.ArticleQueryDTO;
import com.swimmingsys.model.dto.ArticleUpdateDTO;
import com.swimmingsys.model.entity.*;
import com.swimmingsys.model.vo.ArticleVO;
import com.swimmingsys.service.ArticleService;
import com.swimmingsys.utils.Md5Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 文章资料服务实现类
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticleAttachmentMapper articleAttachmentMapper;

    @Resource
    private ArticleCategoryMapper articleCategoryMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional
    public ArticleVO createArticle(ArticleAddDTO addDTO, User loginUser) {
        // 验证权限
        if (loginUser.getRole() != RoleConstant.ADMIN) {
            throw new RuntimeException("仅管理员可上传资料");
        }

        // 创建文章资料实体
        Article article = new Article();
        article.setTitle(addDTO.getTitle());
        article.setContent(addDTO.getContent());
        article.setSummary(addDTO.getSummary());
        article.setCoverImage(addDTO.getCoverImage());
        article.setCategoryId(addDTO.getCategoryId());
        article.setAuthorId(loginUser.getId());
        article.setStatus(addDTO.getStatus());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setIsTop(addDTO.getIsTop() != null ? (addDTO.getIsTop() ? 1 : 0) : 0);
        article.setIsFeatured(addDTO.getIsFeatured() != null ? (addDTO.getIsFeatured() ? 1 : 0) : 0);
        article.setCreatedTime(LocalDateTime.now());
        article.setUpdatedTime(LocalDateTime.now());
        article.setIsDelete(0);

        // 保存文章资料
        articleMapper.insert(article);

        // 返回VO对象
        return convertToVO(article, loginUser.getUserName(), getCategoryName(addDTO.getCategoryId()));
    }

    @Override
    public ArticleVO getArticleById(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("文章资料ID无效");
        }

        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsDelete() == 1) {
            throw new RuntimeException("文章资料不存在");
        }

        // 获取作者信息
        User author = userMapper.selectById(article.getAuthorId());
        String authorName = author != null ? author.getUserName() : "未知";

        // 获取分类信息
        String categoryName = getCategoryName(article.getCategoryId());

        return convertToVO(article, authorName, categoryName);
    }

    @Override
    public Page<ArticleVO> getArticleList(ArticleQueryDTO queryDTO) {
        // 构建查询条件
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0); // 排除已删除的记录

        // 标题模糊查询
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
            queryWrapper.like("title", queryDTO.getTitle().trim());
        }

        // 分类ID查询
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq("category_id", queryDTO.getCategoryId());
        }

        // 状态查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }

        // 置顶查询
        if (queryDTO.getIsTop() != null) {
            queryWrapper.eq("is_top", queryDTO.getIsTop() ? 1 : 0);
        }

        // 推荐查询
        if (queryDTO.getIsFeatured() != null) {
            queryWrapper.eq("is_featured", queryDTO.getIsFeatured() ? 1 : 0);
        }

        // 时间范围查询
        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge("created_time", queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le("created_time", queryDTO.getEndTime());
        }

        // 按创建时间降序排列
        queryWrapper.orderByDesc("created_time");

        // 分页查询
        Page<Article> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Article> resultPage = articleMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        Page<ArticleVO> voPage = new Page<>();
        voPage.setCurrent(resultPage.getCurrent());
        voPage.setSize(resultPage.getSize());
        voPage.setTotal(resultPage.getTotal());

        List<ArticleVO> voList = resultPage.getRecords().stream().map(article -> {
            // 获取作者信息
            User author = userMapper.selectById(article.getAuthorId());
            String authorName = author != null ? author.getUserName() : "未知";

            // 获取分类信息
            String categoryName = getCategoryName(article.getCategoryId());

            return convertToVO(article, authorName, categoryName);
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public ArticleVO updateArticle(Long id, ArticleUpdateDTO updateDTO, User loginUser) {
        if (id == null || id <= 0) {
            throw new RuntimeException("文章资料ID无效");
        }

        // 验证权限 - 只有管理员可以编辑
        if (loginUser.getRole() != RoleConstant.ADMIN) {
            throw new RuntimeException("仅管理员可编辑资料");
        }

        // 查询文章资料
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsDelete() == 1) {
            throw new RuntimeException("文章资料不存在");
        }

        // 验证是否为当前用户创建（如果是管理员则跳过此验证）
        if (!loginUser.getId().equals(article.getAuthorId()) && loginUser.getRole() != RoleConstant.ADMIN) {
            throw new RuntimeException("无权编辑此文章资料");
        }

        // 更新文章资料信息
        if (updateDTO.getTitle() != null) {
            article.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getContent() != null) {
            article.setContent(updateDTO.getContent());
        }
        if (updateDTO.getSummary() != null) {
            article.setSummary(updateDTO.getSummary());
        }
        if (updateDTO.getCoverImage() != null) {
            article.setCoverImage(updateDTO.getCoverImage());
        }
        if (updateDTO.getCategoryId() != null) {
            article.setCategoryId(updateDTO.getCategoryId());
        }
        if (updateDTO.getStatus() != null) {
            article.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getIsTop() != null) {
            article.setIsTop(updateDTO.getIsTop() ? 1 : 0);
        }
        if (updateDTO.getIsFeatured() != null) {
            article.setIsFeatured(updateDTO.getIsFeatured() ? 1 : 0);
        }
        article.setUpdatedTime(LocalDateTime.now());

        // 更新数据库
        articleMapper.updateById(article);

        // 获取作者信息
        String authorName = loginUser.getUserName();

        // 获取分类信息
        String categoryName = getCategoryName(updateDTO.getCategoryId());

        return convertToVO(article, authorName, categoryName);
    }

    @Override
    @Transactional
    public boolean deleteArticle(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new RuntimeException("文章资料ID无效");
        }

        // 验证权限 - 只有管理员可以删除
        if (loginUser.getRole() != RoleConstant.ADMIN) {
            throw new RuntimeException("仅管理员可删除资料");
        }

        // 查询文章资料
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsDelete() == 1) {
            throw new RuntimeException("文章资料不存在");
        }

        // 验证是否为当前用户创建（如果是管理员则跳过此验证）
        if (!loginUser.getId().equals(article.getAuthorId()) && loginUser.getRole() != RoleConstant.ADMIN) {
            throw new RuntimeException("无权删除此文章资料");
        }

        // 逻辑删除
        article.setIsDelete(1);
        article.setUpdatedTime(LocalDateTime.now());
        int result = articleMapper.updateById(article);

        return result > 0;
    }

    @Override
    @Transactional
    public boolean publishArticle(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new RuntimeException("文章资料ID无效");
        }

        // 验证权限 - 只有管理员可以发布
        if (loginUser.getRole() != RoleConstant.ADMIN) {
            throw new RuntimeException("仅管理员可发布资料");
        }

        // 查询文章资料
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsDelete() == 1) {
            throw new RuntimeException("文章资料不存在");
        }

        // 设置为发布状态
        article.setStatus(1); // 发布状态
        article.setUpdatedTime(LocalDateTime.now());
        int result = articleMapper.updateById(article);

        return result > 0;
    }

    @Override
    @Transactional
    public boolean unpublishArticle(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new RuntimeException("文章资料ID无效");
        }

        // 验证权限 - 只有管理员可以下架
        if (loginUser.getRole() != RoleConstant.ADMIN) {
            throw new RuntimeException("仅管理员可下架资料");
        }

        // 查询文章资料
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsDelete() == 1) {
            throw new RuntimeException("文章资料不存在");
        }

        // 设置为隐藏状态
        article.setStatus(2); // 隐藏状态
        article.setUpdatedTime(LocalDateTime.now());
        int result = articleMapper.updateById(article);

        return result > 0;
    }

    @Override
    public Page<ArticleVO> getPublicArticleList(ArticleQueryDTO queryDTO) {
        // 构建查询条件 - 只查询已发布的文章
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0); // 排除已删除的记录
        queryWrapper.eq("status", 1); // 只查询已发布的文章

        // 标题模糊查询
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
            queryWrapper.like("title", queryDTO.getTitle().trim());
        }

        // 分类ID查询
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq("category_id", queryDTO.getCategoryId());
        }

        // 置顶查询
        if (queryDTO.getIsTop() != null) {
            queryWrapper.eq("is_top", queryDTO.getIsTop() ? 1 : 0);
        }

        // 推荐查询
        if (queryDTO.getIsFeatured() != null) {
            queryWrapper.eq("is_featured", queryDTO.getIsFeatured() ? 1 : 0);
        }

        // 时间范围查询
        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge("created_time", queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le("created_time", queryDTO.getEndTime());
        }

        // 按置顶和创建时间排序（置顶的文章排在前面，然后按时间倒序）
        queryWrapper.orderByDesc("is_top").orderByDesc("created_time");

        // 分页查询
        Page<Article> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Article> resultPage = articleMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        Page<ArticleVO> voPage = new Page<>();
        voPage.setCurrent(resultPage.getCurrent());
        voPage.setSize(resultPage.getSize());
        voPage.setTotal(resultPage.getTotal());

        List<ArticleVO> voList = resultPage.getRecords().stream().map(article -> {
            // 获取作者信息
            User author = userMapper.selectById(article.getAuthorId());
            String authorName = author != null ? author.getUserName() : "未知";

            // 获取分类信息
            String categoryName = getCategoryName(article.getCategoryId());

            return convertToVO(article, authorName, categoryName);
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public boolean increaseViewCount(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("文章资料ID无效");
        }

        // 查询文章资料
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsDelete() == 1) {
            throw new RuntimeException("文章资料不存在");
        }

        // 更新浏览次数
        article.setViewCount(article.getViewCount() + 1);
        article.setUpdatedTime(LocalDateTime.now());
        int result = articleMapper.updateById(article);

        return result > 0;
    }

    @Override
    @Transactional
    public boolean toggleLike(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new RuntimeException("文章资料ID无效");
        }

        // 查询文章资料
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsDelete() == 1) {
            throw new RuntimeException("文章资料不存在");
        }

        // 更新点赞数（这里简单地每次调用就增加一次，实际应用中应该记录用户点赞状态）
        article.setLikeCount(article.getLikeCount() + 1);
        article.setUpdatedTime(LocalDateTime.now());
        int result = articleMapper.updateById(article);

        return result > 0;
    }

    /**
     * 将文章资料实体转换为VO
     *
     * @param article 文章资料实体
     * @param authorName 作者名称
     * @param categoryName 分类名称
     * @return 文章资料VO
     */
    private ArticleVO convertToVO(Article article, String authorName, String categoryName) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setCategoryId(article.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setAuthorId(article.getAuthorId());
        vo.setAuthorName(authorName);
        vo.setStatus(article.getStatus());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCommentCount(article.getCommentCount());
        vo.setIsTop(article.getIsTop());
        vo.setIsFeatured(article.getIsFeatured());
        vo.setCreatedTime(article.getCreatedTime());
        vo.setUpdatedTime(article.getUpdatedTime());
        return vo;
    }

    /**
     * 根据分类ID获取分类名称
     *
     * @param categoryId 分类ID
     * @return 分类名称
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        ArticleCategory category = articleCategoryMapper.selectById(categoryId);
        return category != null ? category.getCategoryName() : null;
    }
}