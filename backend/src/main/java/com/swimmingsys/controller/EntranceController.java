package com.swimmingsys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.common.Result;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.annotation.AuthCheck;
import com.swimmingsys.model.dto.EntranceRecordQueryDTO;
import com.swimmingsys.model.dto.EntranceVerifyDTO;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.EntranceQrcodeVO;
import com.swimmingsys.model.vo.EntranceRecordVO;
import com.swimmingsys.model.vo.EntranceVerifyResultVO;
import com.swimmingsys.service.EntranceService;
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
 * 入场管理控制器
 * 提供入场二维码生成、验证和记录查询等功能
 */
@Api(tags = "入场管理")
@RestController
@RequestMapping("/api/v1/entrance")
@Validated
public class EntranceController {

    @Resource
    private EntranceService entranceService;

    @Resource
    private UserService userService;

    /**
     * 生成入场二维码
     * 会员手动点击生成，包含用户信息和可选的课程预约信息
     *
     * @param request 请求对象（用于获取登录用户）
     * @return 二维码信息
     */
    @ApiOperation("生成入场二维码")
    @PostMapping("/generate")
    @AuthCheck(mustRole = RoleConstant.MEMBER)
    public Result<EntranceQrcodeVO> generateQrcode(HttpServletRequest request) {
        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);
            EntranceQrcodeVO qrcodeVO = entranceService.generateEntranceQrcode(loginUser);
            return Result.success("生成入场码成功", qrcodeVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 扫码验证入场
     * 管理员扫描二维码验证会员身份并生成入场记录
     *
     * @param verifyDTO 验证请求（包含二维码令牌）
     * @param request   请求对象（用于获取验证人信息）
     * @return 验证结果（包含会员信息和课程提示）
     */
    @ApiOperation("扫码验证入场")
    @PostMapping("/verify")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<EntranceVerifyResultVO> verifyEntrance(
            @Valid @RequestBody EntranceVerifyDTO verifyDTO,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (verifyDTO == null) {
            return Result.error("验证信息不能为空");
        }
        try {
            // 通过JWT令牌获取登录用户（验证人）
            User verifier = userService.getLoginUser(request);
            EntranceVerifyResultVO resultVO = entranceService.verifyEntrance(verifyDTO, verifier);
            return Result.success("验证成功，允许入场", resultVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的入场记录
     * 会员查看个人入场历史记录
     *
     * @param queryDTO 查询条件（分页、时间范围）
     * @param request  请求对象（用于获取登录用户）
     * @return 入场记录分页列表
     */
    @ApiOperation("获取我的入场记录")
    @GetMapping("/my")
    @AuthCheck(mustRole = RoleConstant.MEMBER)
    public Result<IPage<EntranceRecordVO>> getMyRecords(
            @Valid EntranceRecordQueryDTO queryDTO,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (queryDTO == null) {
            queryDTO = new EntranceRecordQueryDTO();
        }
        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);
            IPage<EntranceRecordVO> recordPage = entranceService.getMyEntranceRecords(queryDTO, loginUser);
            return Result.success("查询成功", recordPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有入场记录（管理员）
     * 管理员查看所有会员的入场记录
     *
     * @param queryDTO 查询条件（分页、用户ID、时间范围）
     * @return 入场记录分页列表
     */
    @ApiOperation("获取所有入场记录（管理员）")
    @GetMapping
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<IPage<EntranceRecordVO>> getAllRecords(@Valid EntranceRecordQueryDTO queryDTO) {
        // Controller层参数非空判断
        if (queryDTO == null) {
            queryDTO = new EntranceRecordQueryDTO();
        }
        try {
            IPage<EntranceRecordVO> recordPage = entranceService.getAllEntranceRecords(queryDTO);
            return Result.success("查询成功", recordPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取入场记录详情
     * 管理员和会员可查看入场记录详情
     *
     * @param id 记录ID
     * @return 入场记录信息
     */
    @ApiOperation("根据ID获取入场记录详情")
    @GetMapping("/{id}")
    @AuthCheck(anyRole = {RoleConstant.ADMIN, RoleConstant.MEMBER})
    public Result<EntranceRecordVO> getRecordById(
            @ApiParam(value = "记录ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("记录ID无效");
        }
        try {
            EntranceRecordVO recordVO = entranceService.getEntranceRecordById(id);
            return Result.success("获取成功", recordVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除入场记录（管理员）
     * 逻辑删除，不物理删除数据
     *
     * @param id 记录ID
     * @return 删除结果
     */
    @ApiOperation("删除入场记录（管理员）")
    @DeleteMapping("/{id}")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<Boolean> deleteRecord(
            @ApiParam(value = "记录ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("记录ID无效");
        }
        try {
            boolean result = entranceService.deleteEntranceRecord(id);
            return Result.success("删除成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
