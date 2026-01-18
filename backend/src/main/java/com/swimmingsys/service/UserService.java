package com.swimmingsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.model.dto.UserAddDTO;
import com.swimmingsys.model.dto.UserLoginDTO;
import com.swimmingsys.model.dto.UserQueryDTO;
import com.swimmingsys.model.dto.UserRegisterDTO;
import com.swimmingsys.model.dto.UserUpdateDTO;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    UserVO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 用户信息（包含JWT令牌）
     */
    UserVO login(UserLoginDTO loginDTO);

    /**
     * 获取当前登录用户
     * 从HTTP请求中提取JWT令牌，验证令牌有效性并返回用户信息
     *
     * @param request HTTP请求对象
     * @return 当前登录的用户实体
     * @throws RuntimeException 如果令牌无效、已过期或用户不存在
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取用户列表（分页/条件查询）
     * 仅管理员可调用
     *
     * @param queryDTO 查询条件
     * @return 用户分页列表
     */
    IPage<UserVO> getUserList(UserQueryDTO queryDTO);

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 新增用户
     * 仅管理员可调用
     *
     * @param addDTO 新增用户信息
     * @return 新增的用户信息
     */
    UserVO addUser(UserAddDTO addDTO);

    /**
     * 更新用户信息
     *
     * @param id        用户ID
     * @param updateDTO 更新信息
     * @param loginUser 当前登录用户
     * @return 更新后的用户信息
     */
    UserVO updateUser(Long id, UserUpdateDTO updateDTO, User loginUser);

    /**
     * 删除用户（逻辑删除）
     * 仅管理员可调用
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long id);
}
