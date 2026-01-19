package com.swimmingsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swimmingsys.common.ResultCode;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.exception.BusinessException;
import com.swimmingsys.common.exception.UnauthorizedException;
import com.swimmingsys.mapper.UserMapper;
import com.swimmingsys.model.dto.UserAddDTO;
import com.swimmingsys.model.dto.UserLoginDTO;
import com.swimmingsys.model.dto.UserQueryDTO;
import com.swimmingsys.model.dto.UserRegisterDTO;
import com.swimmingsys.model.dto.UserUpdateDTO;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.UserVO;
import com.swimmingsys.service.UserService;
import com.swimmingsys.utils.JwtUtil;
import com.swimmingsys.utils.Md5Util;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private com.swimmingsys.service.StatisticsService statisticsService;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    @Override
    public UserVO register(UserRegisterDTO registerDTO) {
        // 1. 校验参数非空
        if (registerDTO == null) {
            throw new RuntimeException("注册信息不能为空");
        }
        if (registerDTO.getUserAccount() == null || registerDTO.getUserAccount().trim().isEmpty()) {
            throw new RuntimeException("用户账号不能为空");
        }
        if (registerDTO.getPassword() == null || registerDTO.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (registerDTO.getConfirmPassword() == null || registerDTO.getConfirmPassword().trim().isEmpty()) {
            throw new RuntimeException("确认密码不能为空");
        }
        if (registerDTO.getUserName() == null || registerDTO.getUserName().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (registerDTO.getPhone() == null || registerDTO.getPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号码不能为空");
        }
        if (registerDTO.getEmail() == null || registerDTO.getEmail().trim().isEmpty()) {
            throw new RuntimeException("邮箱地址不能为空");
        }
        if (registerDTO.getGender() == null) {
            throw new RuntimeException("性别不能为空");
        }

        String userAccount = registerDTO.getUserAccount().trim();
        String password = registerDTO.getPassword().trim();
        String confirmPassword = registerDTO.getConfirmPassword().trim();
        String userName = registerDTO.getUserName().trim();
        String phone = registerDTO.getPhone().trim();
        String email = registerDTO.getEmail().trim();
        Integer gender = registerDTO.getGender();

        // 2. 校验用户账号格式（4-16位字母、数字、下划线）
        if (!userAccount.matches("^[a-zA-Z0-9_]{4,16}$")) {
            throw new RuntimeException("用户账号格式不正确，只能包含4-16位字母、数字、下划线");
        }

        // 3. 校验密码长度（至少6位）
        if (password.length() < 6 || password.length() > 32) {
            throw new RuntimeException("密码长度必须在6-32位之间");
        }

        // 4. 校验两次密码是否一致
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("两次密码输入不一致");
        }

        // 5. 校验手机号格式（11位数字，1开头）
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号码格式不正确");
        }

        // 6. 校验邮箱格式
        if (!email.matches("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$")) {
            throw new RuntimeException("邮箱地址格式不正确");
        }

        // 7. 校验性别值范围（0-2）
        if (gender < 0 || gender > 2) {
            throw new RuntimeException("性别值无效，必须为0-2之间");
        }

        // 8. 检查账号是否已存在
        QueryWrapper<User> accountQueryWrapper = new QueryWrapper<>();
        accountQueryWrapper.eq("user_account", userAccount);
        Long accountCount = userMapper.selectCount(accountQueryWrapper);
        if (accountCount > 0) {
            throw new RuntimeException(ResultCode.USER_EXIST.getMessage());
        }

        // 9. 检查手机号是否已存在
        QueryWrapper<User> phoneQueryWrapper = new QueryWrapper<>();
        phoneQueryWrapper.eq("phone", phone);
        Long phoneCount = userMapper.selectCount(phoneQueryWrapper);
        if (phoneCount > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        // 10. 检查邮箱是否已存在
        QueryWrapper<User> emailQueryWrapper = new QueryWrapper<>();
        emailQueryWrapper.eq("email", email);
        Long emailCount = userMapper.selectCount(emailQueryWrapper);
        if (emailCount > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 11. 创建用户对象
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(Md5Util.encrypt(password)); // MD5加密密码
        user.setUserName(userName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setGender(gender);
        user.setRole(2); // 默认非会员
        user.setStatus(1); // 默认启用

        // 12. 插入数据库
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("注册失败");
        }

        // 13. 清除会员统计缓存和运营概览缓存
        try {
            statisticsService.clearMemberCache();
            statisticsService.clearDashboardCache();
        } catch (Exception e) {
            // 缓存清除失败不影响主业务
        }

        // 14. 返回用户信息（不包含密码）
        return convertToUserVO(user);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 用户信息（包含JWT令牌）
     */
    @Override
    public UserVO login(UserLoginDTO loginDTO) {
        // 1. 校验参数非空
        if (loginDTO == null) {
            throw new RuntimeException("登录信息不能为空");
        }
        if (loginDTO.getUserAccount() == null || loginDTO.getUserAccount().trim().isEmpty()) {
            throw new RuntimeException("用户账号不能为空");
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        String userAccount = loginDTO.getUserAccount().trim();
        String password = loginDTO.getPassword().trim();

        // 2. 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("is_delete", 0); // 未删除
        User user = userMapper.selectOne(queryWrapper);

        // 3. 校验用户是否存在
        if (user == null) {
            throw new RuntimeException(ResultCode.USER_NOT_EXIST.getMessage());
        }

        // 4. 校验密码是否正确
        String encryptedPassword = Md5Util.encrypt(password);
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new RuntimeException(ResultCode.USER_LOGIN_ERROR.getMessage());
        }

        // 5. 校验用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException(ResultCode.USER_DISABLED.getMessage());
        }

        // 6. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUserAccount(), user.getRole());

        // 7. 返回用户信息（包含令牌）
        UserVO userVO = convertToUserVO(user);
        userVO.setToken(token);
        return userVO;
    }

    /**
     * 将User实体类转换为UserVO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取当前登录用户
     * 从HTTP请求中提取JWT令牌，验证令牌有效性并返回用户信息
     *
     * @param request HTTP请求对象
     * @return 当前登录的用户实体
     * @throws RuntimeException 如果令牌无效、已过期或用户不存在
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1. 从请求头中获取Authorization
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader)) {
            throw new UnauthorizedException("未授权，请先登录");
        }
    
        // 2. 提取token（支持Bearer格式）
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
    
        // 3. 验证token是否有效
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Token无效或已过期，请重新登录");
        }
    
        // 4. 从tok en中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new UnauthorizedException("Token解析失败，无法获取用户信息");
        }
    
        // 5. 根据ID查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST.getCode(), ResultCode.USER_NOT_EXIST.getMessage());
        }
    
        // 6. 校验用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED.getCode(), ResultCode.USER_DISABLED.getMessage());
        }
    
        // 7. 校验逻辑删除状态
        if (user.getIsDelete() == 1) {
            throw new BusinessException(ResultCode.USER_DELETED.getCode(), ResultCode.USER_DELETED.getMessage());
        }
    
        return user;
    }

    /**
     * 获取用户列表（分页/条件查询）
     *
     * @param queryDTO 查询条件
     * @return 用户分页列表
     */
    @Override
    public IPage<UserVO> getUserList(UserQueryDTO queryDTO) {
        // 1. 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        // 用户账号模糊查询
        if (StringUtils.hasText(queryDTO.getUserAccount())) {
            queryWrapper.like("user_account", queryDTO.getUserAccount());
        }
        // 用户名模糊查询
        if (StringUtils.hasText(queryDTO.getUserName())) {
            queryWrapper.like("user_name", queryDTO.getUserName());
        }
        // 手机号模糊查询
        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryWrapper.like("phone", queryDTO.getPhone());
        }
        // 邮箱模糊查询
        if (StringUtils.hasText(queryDTO.getEmail())) {
            queryWrapper.like("email", queryDTO.getEmail());
        }
        // 角色精确查询
        if (queryDTO.getRole() != null) {
            queryWrapper.eq("role", queryDTO.getRole());
        }
        // 状态精确查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }
        // 排除已删除用户
        queryWrapper.eq("is_delete", 0);
        // 按创建时间倒序
        queryWrapper.orderByDesc("created_time");

        // 2. 执行分页查询
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);

        // 3. 转换为VO分页
        IPage<UserVO> userVOPage = userPage.convert(this::convertToUserVO);
        return userVOPage;
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public UserVO getUserById(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("用户ID无效");
        }

        // 2. 查询用户
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDelete() == 1) {
            throw new RuntimeException(ResultCode.USER_NOT_EXIST.getMessage());
        }

        // 3. 转换并返回
        return convertToUserVO(user);
    }

    /**
     * 新增用户
     *
     * @param addDTO 新增用户信息
     * @return 新增的用户信息
     */
    @Override
    public UserVO addUser(UserAddDTO addDTO) {
        // 1. 参数校验
        if (addDTO == null) {
            throw new RuntimeException("用户信息不能为空");
        }

        String userAccount = addDTO.getUserAccount().trim();
        String password = addDTO.getPassword().trim();
        String phone = addDTO.getPhone().trim();
        String email = addDTO.getEmail().trim();

        // 2. 检查账号是否已存在
        QueryWrapper<User> accountQueryWrapper = new QueryWrapper<>();
        accountQueryWrapper.eq("user_account", userAccount);
        Long accountCount = userMapper.selectCount(accountQueryWrapper);
        if (accountCount > 0) {
            throw new RuntimeException(ResultCode.USER_EXIST.getMessage());
        }

        // 3. 检查手机号是否已存在
        QueryWrapper<User> phoneQueryWrapper = new QueryWrapper<>();
        phoneQueryWrapper.eq("phone", phone);
        Long phoneCount = userMapper.selectCount(phoneQueryWrapper);
        if (phoneCount > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        // 4. 检查邮箱是否已存在
        QueryWrapper<User> emailQueryWrapper = new QueryWrapper<>();
        emailQueryWrapper.eq("email", email);
        Long emailCount = userMapper.selectCount(emailQueryWrapper);
        if (emailCount > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 5. 创建用户对象
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(Md5Util.encrypt(password));
        user.setUserName(addDTO.getUserName().trim());
        user.setPhone(phone);
        user.setEmail(email);
        user.setGender(addDTO.getGender());
        user.setAvatar(addDTO.getAvatar());
        user.setRole(addDTO.getRole());
        user.setStatus(addDTO.getStatus());

        // 6. 插入数据库
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("新增用户失败");
        }

        // 7. 返回用户信息
        return convertToUserVO(user);
    }

    /**
     * 更新用户信息
     *
     * @param id        用户ID
     * @param updateDTO 更新信息
     * @param loginUser 当前登录用户
     * @return 更新后的用户信息
     */
    @Override
    public UserVO updateUser(Long id, UserUpdateDTO updateDTO, User loginUser) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("用户ID无效");
        }
        if (updateDTO == null) {
            throw new RuntimeException("更新信息不能为空");
        }

        // 2. 查询用户是否存在
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDelete() == 1) {
            throw new RuntimeException(ResultCode.USER_NOT_EXIST.getMessage());
        }

        // 3. 权限判断：管理员可修改所有用户，普通用户只能修改自己
        boolean isAdmin = loginUser.getRole().equals(RoleConstant.ADMIN);
        boolean isSelf = loginUser.getId().equals(id);
        if (!isAdmin && !isSelf) {
            throw new RuntimeException("无权修改此用户信息");
        }

        // 4. 根据角色更新不同字段
        // 普通用户只能修改：userName, phone, email, gender, avatar
        if (StringUtils.hasText(updateDTO.getUserName())) {
            user.setUserName(updateDTO.getUserName().trim());
        }
        if (StringUtils.hasText(updateDTO.getPhone())) {
            // 检查手机号是否已被其他用户使用
            QueryWrapper<User> phoneQueryWrapper = new QueryWrapper<>();
            phoneQueryWrapper.eq("phone", updateDTO.getPhone().trim());
            phoneQueryWrapper.ne("id", id);
            Long phoneCount = userMapper.selectCount(phoneQueryWrapper);
            if (phoneCount > 0) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
            user.setPhone(updateDTO.getPhone().trim());
        }
        if (StringUtils.hasText(updateDTO.getEmail())) {
            // 检查邮箱是否已被其他用户使用
            QueryWrapper<User> emailQueryWrapper = new QueryWrapper<>();
            emailQueryWrapper.eq("email", updateDTO.getEmail().trim());
            emailQueryWrapper.ne("id", id);
            Long emailCount = userMapper.selectCount(emailQueryWrapper);
            if (emailCount > 0) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            user.setEmail(updateDTO.getEmail().trim());
        }
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }

        // 仅管理员可修改的字段：role, status, password
        if (isAdmin) {
            if (updateDTO.getRole() != null) {
                user.setRole(updateDTO.getRole());
            }
            if (updateDTO.getStatus() != null) {
                user.setStatus(updateDTO.getStatus());
            }
            if (StringUtils.hasText(updateDTO.getPassword())) {
                user.setPassword(Md5Util.encrypt(updateDTO.getPassword().trim()));
            }
        }

        // 5. 更新数据库
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new RuntimeException("更新用户信息失败");
        }

        // 6. 返回更新后的用户信息
        return convertToUserVO(user);
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteUser(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("用户ID无效");
        }

        // 2. 查询用户是否存在
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDelete() == 1) {
            throw new RuntimeException(ResultCode.USER_NOT_EXIST.getMessage());
        }

        // 3. 逻辑删除（MyBatis-Plus会自动处理@TableLogic注解）
        int result = userMapper.deleteById(id);
        return result > 0;
    }
}
