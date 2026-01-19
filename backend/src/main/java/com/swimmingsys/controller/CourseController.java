package com.swimmingsys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.common.Result;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.annotation.AuthCheck;
import com.swimmingsys.model.dto.CourseAddDTO;
import com.swimmingsys.model.dto.CourseQueryDTO;
import com.swimmingsys.model.dto.CourseUpdateDTO;
import com.swimmingsys.model.vo.CourseVO;
import com.swimmingsys.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 课程控制器
 * 提供课程管理相关的REST API接口
 */
@Api(tags = "课程管理")
@RestController
@RequestMapping("/api/v1/courses")
@Validated
public class CourseController {

    @Resource
    private CourseService courseService;

    /**
     * 获取课程列表（分页/条件查询）
     * 管理员和会员可调用
     *
     * @param queryDTO 查询条件
     * @return 课程分页列表
     */
    @ApiOperation("获取课程列表（分页/条件查询）")
    @GetMapping
    @AuthCheck(anyRole = {RoleConstant.ADMIN, RoleConstant.MEMBER, RoleConstant.NON_MEMBER})
    public Result<IPage<CourseVO>> getCourseList(@Valid CourseQueryDTO queryDTO) {
        // Controller层参数非空判断
        if (queryDTO == null) {
            queryDTO = new CourseQueryDTO();
        }
        try {
            IPage<CourseVO> coursePage = courseService.getCourseList(queryDTO);
            return Result.success("查询成功", coursePage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取课程信息
     * 管理员和会员可调用
     *
     * @param id 课程ID
     * @return 课程信息
     */
    @ApiOperation("根据ID获取课程信息")
    @GetMapping("/{id}")
    @AuthCheck(anyRole = {RoleConstant.ADMIN, RoleConstant.MEMBER, RoleConstant.NON_MEMBER})
    public Result<CourseVO> getCourseById(
            @ApiParam(value = "课程ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("课程ID无效");
        }
        try {
            CourseVO courseVO = courseService.getCourseById(id);
            return Result.success("获取成功", courseVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 新增课程
     * 仅管理员可调用
     *
     * @param addDTO 新增课程信息
     * @return 新增的课程信息
     */
    @ApiOperation("新增课程")
    @PostMapping
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<CourseVO> addCourse(@Valid @RequestBody CourseAddDTO addDTO) {
        // Controller层参数非空判断
        if (addDTO == null) {
            return Result.error("课程信息不能为空");
        }
        try {
            CourseVO courseVO = courseService.addCourse(addDTO);
            return Result.success("新增课程成功", courseVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新课程信息
     * 仅管理员可调用
     *
     * @param id        课程ID
     * @param updateDTO 更新信息
     * @return 更新后的课程信息
     */
    @ApiOperation("更新课程信息")
    @PutMapping("/{id}")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<CourseVO> updateCourse(
            @ApiParam(value = "课程ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CourseUpdateDTO updateDTO) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("课程ID无效");
        }
        if (updateDTO == null) {
            return Result.error("更新信息不能为空");
        }
        try {
            CourseVO courseVO = courseService.updateCourse(id, updateDTO);
            return Result.success("更新成功", courseVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除课程（逻辑删除）
     * 仅管理员可调用
     *
     * @param id 课程ID
     * @return 是否删除成功
     */
    @ApiOperation("删除课程（逻辑删除）")
    @DeleteMapping("/{id}")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<Boolean> deleteCourse(
            @ApiParam(value = "课程ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("课程ID无效");
        }
        try {
            boolean result = courseService.deleteCourse(id);
            return Result.success("删除成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改课程状态（发布/下架）
     * 仅管理员可调用
     *
     * @param id     课程ID
     * @param status 状态：0-已下架，1-已发布
     * @return 更新后的课程信息
     */
    @ApiOperation("修改课程状态（发布/下架）")
    @PutMapping("/{id}/status")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<CourseVO> updateCourseStatus(
            @ApiParam(value = "课程ID", required = true) @PathVariable Long id,
            @ApiParam(value = "状态：0-已下架，1-已发布", required = true) @RequestParam Integer status) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("课程ID无效");
        }
        if (status == null) {
            return Result.error("状态值不能为空");
        }
        try {
            CourseVO courseVO = courseService.updateCourseStatus(id, status);
            return Result.success("状态更新成功", courseVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
