package com.swimmingsys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.common.Result;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.annotation.AuthCheck;
import com.swimmingsys.model.dto.CoachAddDTO;
import com.swimmingsys.model.dto.CoachQueryDTO;
import com.swimmingsys.model.dto.CoachUpdateDTO;
import com.swimmingsys.model.vo.CoachVO;
import com.swimmingsys.service.CoachService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 教练控制器
 * 提供教练管理相关的REST API接口
 */
@Api(tags = "教练管理")
@RestController
@RequestMapping("/api/v1/coaches")
@Validated
public class CoachController {

    @Resource
    private CoachService coachService;

    /**
     * 获取教练列表（分页/条件查询）
     * 仅管理员可调用
     *
     * @param queryDTO 查询条件
     * @return 教练分页列表
     */
    @ApiOperation("获取教练列表（分页/条件查询）")
    @GetMapping
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<IPage<CoachVO>> getCoachList(@Valid CoachQueryDTO queryDTO) {
        // Controller层参数非空判断
        if (queryDTO == null) {
            queryDTO = new CoachQueryDTO();
        }
        try {
            IPage<CoachVO> coachPage = coachService.getCoachList(queryDTO);
            return Result.success("查询成功", coachPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取教练信息
     * 管理员和登录用户可调用（用于课程预约时查看教练信息）
     *
     * @param id 教练ID
     * @return 教练信息
     */
    @ApiOperation("根据ID获取教练信息")
    @GetMapping("/{id}")
    @AuthCheck(anyRole = {RoleConstant.ADMIN, RoleConstant.MEMBER, RoleConstant.NON_MEMBER})
    public Result<CoachVO> getCoachById(
            @ApiParam(value = "教练ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("教练ID无效");
        }
        try {
            CoachVO coachVO = coachService.getCoachById(id);
            return Result.success("获取成功", coachVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 新增教练
     * 仅管理员可调用
     *
     * @param addDTO 新增教练信息
     * @return 新增的教练信息
     */
    @ApiOperation("新增教练")
    @PostMapping
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<CoachVO> addCoach(@Valid @RequestBody CoachAddDTO addDTO) {
        // Controller层参数非空判断
        if (addDTO == null) {
            return Result.error("教练信息不能为空");
        }
        try {
            CoachVO coachVO = coachService.addCoach(addDTO);
            return Result.success("新增教练成功", coachVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新教练信息
     * 仅管理员可调用
     *
     * @param id        教练ID
     * @param updateDTO 更新信息
     * @return 更新后的教练信息
     */
    @ApiOperation("更新教练信息")
    @PutMapping("/{id}")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<CoachVO> updateCoach(
            @ApiParam(value = "教练ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CoachUpdateDTO updateDTO) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("教练ID无效");
        }
        if (updateDTO == null) {
            return Result.error("更新信息不能为空");
        }
        try {
            CoachVO coachVO = coachService.updateCoach(id, updateDTO);
            return Result.success("更新成功", coachVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除教练（逻辑删除）
     * 仅管理员可调用
     *
     * @param id 教练ID
     * @return 是否删除成功
     */
    @ApiOperation("删除教练（逻辑删除）")
    @DeleteMapping("/{id}")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<Boolean> deleteCoach(
            @ApiParam(value = "教练ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("教练ID无效");
        }
        try {
            boolean result = coachService.deleteCoach(id);
            return Result.success("删除成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取在职教练列表（用于下拉选择）
     * 管理员和登录用户可调用（用于课程关联）
     *
     * @return 在职教练列表
     */
    @ApiOperation("获取在职教练列表（用于下拉选择）")
    @GetMapping("/active")
    @AuthCheck(anyRole = {RoleConstant.ADMIN, RoleConstant.MEMBER, RoleConstant.NON_MEMBER})
    public Result<List<CoachVO>> getActiveCoachList() {
        try {
            List<CoachVO> coachList = coachService.getActiveCoachList();
            return Result.success("获取成功", coachList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
