package com.swimmingsys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.common.Result;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.annotation.AuthCheck;
import com.swimmingsys.model.dto.UserAddDTO;
import com.swimmingsys.model.dto.UserLoginDTO;
import com.swimmingsys.model.dto.UserQueryDTO;
import com.swimmingsys.model.dto.UserRegisterDTO;
import com.swimmingsys.model.dto.UserUpdateDTO;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.UserVO;
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
 * 用户控制器
 * 提供用户管理相关的REST API接口
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 统一响应结果
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        // Controller层参数非空判断
        if (registerDTO == null) {
            throw new RuntimeException("注册信息不能为空");
        }
        try {
            UserVO userVO = userService.register(registerDTO);
            return Result.success("注册成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 统一响应结果（包含JWT令牌）
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        // Controller层参数非空判断
        if (loginDTO == null) {
            throw new RuntimeException("登录信息不能为空");
        }
        try {
            UserVO userVO = userService.login(loginDTO);
            return Result.success("登录成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            UserVO userVO = new UserVO();
            org.springframework.beans.BeanUtils.copyProperties(loginUser, userVO);
            return Result.success("获取成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户列表（分页/条件查询）
     * 仅管理员可调用
     *
     * @param queryDTO 查询条件
     * @return 用户分页列表
     */
    @ApiOperation("获取用户列表（分页/条件查询）")
    @GetMapping
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<IPage<UserVO>> getUserList(@Valid UserQueryDTO queryDTO) {
        // Controller层参数非空判断
        if (queryDTO == null) {
            queryDTO = new UserQueryDTO();
        }
        try {
            IPage<UserVO> userPage = userService.getUserList(queryDTO);
            return Result.success("查询成功", userPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取用户信息
     * 管理员可查看所有用户，普通用户只能查看自己
     *
     * @param id      用户ID
     * @param request HTTP请求对象
     * @return 用户信息
     */
    @ApiOperation("根据ID获取用户信息")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("用户ID无效");
        }
        try {
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);
            // 权限判断：管理员可查看所有，普通用户只能查看自己
            boolean isAdmin = loginUser.getRole().equals(RoleConstant.ADMIN);
            boolean isSelf = loginUser.getId().equals(id);
            if (!isAdmin && !isSelf) {
                return Result.error("无权查看此用户信息");
            }
            UserVO userVO = userService.getUserById(id);
            return Result.success("获取成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 新增用户
     * 仅管理员可调用
     *
     * @param addDTO 新增用户信息
     * @return 新增的用户信息
     */
    @ApiOperation("新增用户")
    @PostMapping
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<UserVO> addUser(@Valid @RequestBody UserAddDTO addDTO) {
        // Controller层参数非空判断
        if (addDTO == null) {
            return Result.error("用户信息不能为空");
        }
        try {
            UserVO userVO = userService.addUser(addDTO);
            return Result.success("新增用户成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     * 管理员可修改所有字段，普通用户只能修改部分字段
     *
     * @param id        用户ID
     * @param updateDTO 更新信息
     * @param request   HTTP请求对象
     * @return 更新后的用户信息
     */
    @ApiOperation("更新用户信息")
    @PutMapping("/{id}")
    public Result<UserVO> updateUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("用户ID无效");
        }
        if (updateDTO == null) {
            return Result.error("更新信息不能为空");
        }
        try {
            User loginUser = userService.getLoginUser(request);
            UserVO userVO = userService.updateUser(id, updateDTO, loginUser);
            return Result.success("更新成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户（逻辑删除）
     * 仅管理员可调用
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    @ApiOperation("删除用户（逻辑删除）")
    @DeleteMapping("/{id}")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<Boolean> deleteUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("用户ID无效");
        }
        try {
            boolean result = userService.deleteUser(id);
            return Result.success("删除成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
