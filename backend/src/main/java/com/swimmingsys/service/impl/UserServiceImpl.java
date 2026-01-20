package com.swimmingsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swimmingsys.common.ResultCode;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.exception.BusinessException;
import com.swimmingsys.common.exception.UnauthorizedException;
import com.swimmingsys.mapper.UserMapper;
import com.swimmingsys.model.dto.UserAddDTO;
import com.swimmingsys.model.dto.UserBatchExpirationDTO;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

        // 6. 校验用户是否过期
        if (isUserExpired(user)) {
            throw new RuntimeException("账户已过期，请联系管理员续费");
        }

        // 7. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUserAccount(), user.getRole());

        // 8. 返回用户信息（包含令牌）
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
        return convertToUserVO(user, 7);
    }

    /**
     * 将User实体类转换为UserVO（带过期计算）
     *
     * @param user        用户实体
     * @param warningDays 预警天数
     * @return 用户VO
     */
    private UserVO convertToUserVO(User user, int warningDays) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        // 计算过期相关字段
        if (user.getExpirationTime() == null) {
            // 未设置过期时间，表示永久有效
            userVO.setExpired(false);
            userVO.setDaysUntilExpiration(Long.MAX_VALUE);
            userVO.setExpirationWarning(false);
        } else {
            LocalDateTime now = LocalDateTime.now();
            boolean expired = now.isAfter(user.getExpirationTime());
            long days = ChronoUnit.DAYS.between(now, user.getExpirationTime());

            userVO.setExpired(expired);
            userVO.setDaysUntilExpiration(days);
            userVO.setExpirationWarning(!expired && days >= 0 && days <= warningDays);
        }

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

        // 过期状态筛选
        if (StringUtils.hasText(queryDTO.getExpirationStatus())) {
            LocalDateTime now = LocalDateTime.now();
            int warningDays = queryDTO.getWarningDays() != null ? queryDTO.getWarningDays() : 7;

            switch (queryDTO.getExpirationStatus()) {
                case "expired":
                    // 已过期：expiration_time < now
                    queryWrapper.isNotNull("expiration_time");
                    queryWrapper.lt("expiration_time", now);
                    break;
                case "expiring":
                    // 即将过期：now <= expiration_time <= now + warningDays
                    LocalDateTime warningTime = now.plusDays(warningDays);
                    queryWrapper.isNotNull("expiration_time");
                    queryWrapper.ge("expiration_time", now);
                    queryWrapper.le("expiration_time", warningTime);
                    break;
                case "normal":
                    // 正常：expiration_time > now + warningDays 或 expiration_time 为 NULL
                    LocalDateTime normalTime = now.plusDays(warningDays);
                    queryWrapper.and(wrapper -> wrapper
                            .isNull("expiration_time")
                            .or()
                            .gt("expiration_time", normalTime)
                    );
                    break;
                case "all":
                default:
                    // 全部，不添加过期筛选条件
                    break;
            }
        }

        // 排除已删除用户
        queryWrapper.eq("is_delete", 0);
        // 按创建时间倒序
        queryWrapper.orderByDesc("created_time");

        // 2. 执行分页查询
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);

        // 3. 转换为VO分页（包含过期状态计算）
        int warningDays = queryDTO.getWarningDays() != null ? queryDTO.getWarningDays() : 7;
        IPage<UserVO> userVOPage = userPage.convert(user -> convertToUserVO(user, warningDays));
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
        user.setExpirationTime(addDTO.getExpirationTime());

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

        // 仅管理员可修改的字段：role, status, password, expirationTime
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
            // 管理员可以设置过期时间（包括设置为NULL清空）
            user.setExpirationTime(updateDTO.getExpirationTime());
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

    // ==================== 过期管理方法实现 ====================

    /**
     * 检查用户是否已过期
     *
     * @param user 用户实体
     * @return 是否已过期
     */
    @Override
    public boolean isUserExpired(User user) {
        if (user == null || user.getExpirationTime() == null) {
            return false; // 未设置过期时间，表示永久有效
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(user.getExpirationTime());
    }

    /**
     * 计算距离过期天数
     *
     * @param user 用户实体
     * @return 距离过期天数（负数表示已过期）
     */
    @Override
    public long getDaysUntilExpiration(User user) {
        if (user == null || user.getExpirationTime() == null) {
            return Long.MAX_VALUE; // 未设置过期时间，返回最大值
        }
        LocalDateTime now = LocalDateTime.now();
        return ChronoUnit.DAYS.between(now, user.getExpirationTime());
    }

    /**
     * 检查并处理过期用户
     * 将过期会员降级为非会员并禁用账户
     *
     * @return 处理结果统计
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkAndHandleExpiredUsers() {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询所有过期的用户（expirationTime < now）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("expiration_time");
        queryWrapper.lt("expiration_time", LocalDateTime.now());
        queryWrapper.eq("status", 1); // 仅处理启用状态的用户
        queryWrapper.eq("is_delete", 0); // 未删除

        List<User> expiredUsers = userMapper.selectList(queryWrapper);
        int totalCount = expiredUsers.size();

        if (totalCount == 0) {
            result.put("success", true);
            result.put("message", "没有需要处理的过期用户");
            result.put("totalCount", 0);
            result.put("processedCount", 0);
            return result;
        }

        // 2. 批量更新过期用户：降级为非会员 + 禁用账户
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.isNotNull("expiration_time");
        updateWrapper.lt("expiration_time", LocalDateTime.now());
        updateWrapper.eq("status", 1);
        updateWrapper.eq("is_delete", 0);
        updateWrapper.set("role", 2); // 降级为非会员
        updateWrapper.set("status", 0); // 禁用账户

        int processedCount = userMapper.update(null, updateWrapper);

        // 3. 清除会员统计缓存
        try {
            statisticsService.clearMemberCache();
            statisticsService.clearDashboardCache();
        } catch (Exception e) {
            // 缓存清除失败不影响主业务
        }

        // 4. 返回处理结果
        result.put("success", true);
        result.put("message", "成功处理过期用户");
        result.put("totalCount", totalCount);
        result.put("processedCount", processedCount);
        result.put("expiredUserIds", expiredUsers.stream().map(User::getId).collect(Collectors.toList()));

        return result;
    }

    /**
     * 获取即将过期的用户列表
     *
     * @param days 预警天数
     * @return 即将过期的用户列表
     */
    @Override
    public List<UserVO> getExpiringUsers(int days) {
        // 1. 参数校验
        if (days < 1) {
            throw new RuntimeException("预警天数必须大于0");
        }

        // 2. 构建查询条件：now <= expiration_time <= now + days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningTime = now.plusDays(days);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("expiration_time");
        queryWrapper.ge("expiration_time", now);
        queryWrapper.le("expiration_time", warningTime);
        queryWrapper.eq("status", 1); // 仅查询启用用户
        queryWrapper.eq("is_delete", 0); // 未删除
        queryWrapper.orderByAsc("expiration_time"); // 按过期时间升序

        // 3. 查询并转换为VO
        List<User> users = userMapper.selectList(queryWrapper);
        return users.stream()
                .map(user -> convertToUserVO(user, days))
                .collect(Collectors.toList());
    }

    /**
     * 批量设置用户过期时间
     *
     * @param batchDTO 批量操作DTO
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchSetExpiration(UserBatchExpirationDTO batchDTO) {
        Map<String, Object> result = new HashMap<>();

        // 1. 参数校验
        if (batchDTO == null || batchDTO.getUserIds() == null || batchDTO.getUserIds().isEmpty()) {
            throw new RuntimeException("用户ID列表不能为空");
        }

        String operationType = batchDTO.getOperationType();
        if (!StringUtils.hasText(operationType)) {
            throw new RuntimeException("操作类型不能为空");
        }

        List<Long> userIds = batchDTO.getUserIds();
        int totalCount = userIds.size();
        int successCount = 0;
        List<String> errors = new ArrayList<>();

        // 2. 根据操作类型处理
        for (Long userId : userIds) {
            try {
                User user = userMapper.selectById(userId);
                if (user == null || user.getIsDelete() == 1) {
                    errors.add("用户ID " + userId + " 不存在或已删除");
                    continue;
                }

                LocalDateTime newExpirationTime = null;

                switch (operationType) {
                    case "set":
                        // 设置固定过期时间
                        newExpirationTime = batchDTO.getExpirationTime();
                        break;

                    case "extend":
                        // 延长过期时间
                        if (batchDTO.getDays() == null || batchDTO.getDays() <= 0) {
                            errors.add("用户ID " + userId + " 延长天数必须大于0");
                            continue;
                        }
                        if (user.getExpirationTime() == null) {
                            // 未设置过期时间，从当前时间开始延长
                            newExpirationTime = LocalDateTime.now().plusDays(batchDTO.getDays());
                        } else {
                            newExpirationTime = user.getExpirationTime().plusDays(batchDTO.getDays());
                        }
                        break;

                    case "shorten":
                        // 缩短过期时间
                        if (batchDTO.getDays() == null || batchDTO.getDays() <= 0) {
                            errors.add("用户ID " + userId + " 缩短天数必须大于0");
                            continue;
                        }
                        if (user.getExpirationTime() == null) {
                            errors.add("用户ID " + userId + " 未设置过期时间，无法缩短");
                            continue;
                        }
                        newExpirationTime = user.getExpirationTime().minusDays(batchDTO.getDays());
                        break;

                    case "clear":
                        // 清空过期时间（设为NULL）
                        newExpirationTime = null;
                        break;

                    default:
                        errors.add("用户ID " + userId + " 不支持的操作类型: " + operationType);
                        continue;
                }

                // 3. 更新数据库
                user.setExpirationTime(newExpirationTime);
                int updateResult = userMapper.updateById(user);
                if (updateResult > 0) {
                    successCount++;
                } else {
                    errors.add("用户ID " + userId + " 更新失败");
                }

            } catch (Exception e) {
                errors.add("用户ID " + userId + " 处理异常: " + e.getMessage());
            }
        }

        // 4. 清除会员统计缓存
        try {
            statisticsService.clearMemberCache();
            statisticsService.clearDashboardCache();
        } catch (Exception e) {
            // 缓存清除失败不影响主业务
        }

        // 5. 返回结果
        result.put("success", errors.isEmpty());
        result.put("message", successCount + "/" + totalCount + " 个用户处理成功");
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", totalCount - successCount);
        result.put("errors", errors);

        return result;
    }
}
