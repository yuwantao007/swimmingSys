package com.swimmingsys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swimmingsys.common.Result;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.annotation.AuthCheck;
import com.swimmingsys.model.dto.ArticleAddDTO;
import com.swimmingsys.model.dto.ArticleQueryDTO;
import com.swimmingsys.model.dto.ArticleUpdateDTO;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.ArticleVO;
import com.swimmingsys.service.ArticleService;
import com.swimmingsys.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 文章资料控制器
 */
@Api(tags = "文章资料管理")
@RestController
@RequestMapping("/api/v1/articles")
@Validated
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private UserService userService;

    /**
     * 上传资料（管理员）
     *
     * @param addDTO 添加信息
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ApiOperation("上传资料（管理员）")
    @PostMapping("/upload")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<ArticleVO> uploadArticle(@Valid @RequestBody ArticleAddDTO addDTO, HttpServletRequest request) {
        // Controller层参数非空判断
        if (addDTO == null) {
            return Result.error("资料信息不能为空");
        }

        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);

            ArticleVO articleVO = articleService.createArticle(addDTO, loginUser);
            return Result.success("上传成功", articleVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取资料列表（分页）- 仅管理员
     *
     * @param queryDTO 查询条件
     * @return 资料分页列表
     */
    @ApiOperation("获取资料列表（分页）- 仅管理员")
    @GetMapping
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<Page<ArticleVO>> getArticleList(@Valid ArticleQueryDTO queryDTO) {
        // Controller层参数非空判断
        if (queryDTO == null) {
            queryDTO = new ArticleQueryDTO();
        }

        try {
            Page<ArticleVO> articlePage = articleService.getArticleList(queryDTO);
            return Result.success("查询成功", articlePage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取资料详情
     *
     * @param id 资料ID
     * @return 资料信息
     */
    @ApiOperation("根据ID获取资料详情")
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleById(
            @ApiParam(value = "资料ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("资料ID无效");
        }

        try {
            ArticleVO articleVO = articleService.getArticleById(id);
            return Result.success("获取成功", articleVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新资料信息（管理员）
     *
     * @param id 资料ID
     * @param updateDTO 更新信息
     * @param request HTTP请求对象
     * @return 更新后的资料信息
     */
    @ApiOperation("更新资料信息（管理员）")
    @PutMapping("/{id}")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<ArticleVO> updateArticle(
            @ApiParam(value = "资料ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ArticleUpdateDTO updateDTO,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("资料ID无效");
        }
        if (updateDTO == null) {
            return Result.error("更新信息不能为空");
        }

        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);

            ArticleVO articleVO = articleService.updateArticle(id, updateDTO, loginUser);
            return Result.success("更新成功", articleVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除资料（管理员）
     *
     * @param id 资料ID
     * @param request HTTP请求对象
     * @return 是否删除成功
     */
    @ApiOperation("删除资料（管理员）")
    @DeleteMapping("/{id}")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<Boolean> deleteArticle(
            @ApiParam(value = "资料ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("资料ID无效");
        }

        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);

            boolean result = articleService.deleteArticle(id, loginUser);
            return Result.success("删除成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布资料（管理员）
     *
     * @param id 资料ID
     * @param request HTTP请求对象
     * @return 是否发布成功
     */
    @ApiOperation("发布资料（管理员）")
    @PutMapping("/{id}/publish")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<Boolean> publishArticle(
            @ApiParam(value = "资料ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("资料ID无效");
        }

        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);

            boolean result = articleService.publishArticle(id, loginUser);
            return Result.success("发布成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 下架资料（管理员）
     *
     * @param id 资料ID
     * @param request HTTP请求对象
     * @return 是否下架成功
     */
    @ApiOperation("下架资料（管理员）")
    @PutMapping("/{id}/unpublish")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<Boolean> unpublishArticle(
            @ApiParam(value = "资料ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("资料ID无效");
        }

        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);

            boolean result = articleService.unpublishArticle(id, loginUser);
            return Result.success("下架成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取公开资料列表（分页）- 所有用户可访问
     *
     * @param queryDTO 查询条件
     * @return 公开资料分页列表
     */
    @ApiOperation("获取公开资料列表（分页）- 所有用户可访问")
    @GetMapping("/public")
    public Result<Page<ArticleVO>> getPublicArticleList(@Valid ArticleQueryDTO queryDTO) {
        // Controller层参数非空判断
        if (queryDTO == null) {
            queryDTO = new ArticleQueryDTO();
        }

        try {
            Page<ArticleVO> articlePage = articleService.getPublicArticleList(queryDTO);
            return Result.success("查询成功", articlePage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 增加浏览次数
     *
     * @param id 资料ID
     * @return 是否增加成功
     */
    @ApiOperation("增加浏览次数")
    @PutMapping("/{id}/view")
    public Result<Boolean> increaseViewCount(
            @ApiParam(value = "资料ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("资料ID无效");
        }

        try {
            boolean result = articleService.increaseViewCount(id);
            return Result.success("浏览次数已增加", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞
     *
     * @param id 资料ID
     * @param request HTTP请求对象
     * @return 点赞状态
     */
    @ApiOperation("点赞/取消点赞")
    @PutMapping("/{id}/like")
    public Result<Boolean> toggleLike(
            @ApiParam(value = "资料ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("资料ID无效");
        }

        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);

            boolean result = articleService.toggleLike(id, loginUser);
            return Result.success("点赞成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}