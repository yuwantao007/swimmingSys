package com.swimmingsys.aop;

import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.annotation.AuthCheck;
import com.swimmingsys.common.exception.ForbiddenException;
import com.swimmingsys.common.exception.UnauthorizedException;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验AOP切面
 * 用于拦截带有@AuthCheck注解的方法，进行角色权限验证
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行权限拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        int mustRole = authCheck.mustRole();
        int[] anyRole = authCheck.anyRole();

        // 获取当前请求
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取当前登录用户（会抛出异常如果未登录或token无效）
        User loginUser;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (RuntimeException e) {
            // 捕获到未授权异常，抛出UnauthorizedException
            throw new UnauthorizedException("请先登录，在请求头中添加Authorization：Bearer <token>");
        }

        Integer userRole = loginUser.getRole();

        // 如果指定了必须拥有的角色
        if (mustRole != -1) {
            if (!userRole.equals(mustRole)) {
                throw new ForbiddenException("权限不足，需要" + getRoleName(mustRole) + "角色");
            }
        }

        // 如果指定了允许的角色列表（满足任意一个即可）
        if (anyRole != null && anyRole.length > 0) {
            boolean hasRole = false;
            for (int role : anyRole) {
                if (userRole.equals(role)) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                throw new ForbiddenException("权限不足，需要以下角色之一：" + getAnyRoleName(anyRole));
            }
        }

        // 权限校验通过，放行
        return joinPoint.proceed();
    }

    /**
     * 获取角色名称
     *
     * @param role 角色代码
     * @return 角色名称
     */
    private String getRoleName(int role) {
        switch (role) {
            case RoleConstant.ADMIN:
                return "管理员";
            case RoleConstant.MEMBER:
                return "会员";
            case RoleConstant.NON_MEMBER:
                return "非会员";
            default:
                return "未知角色";
        }
    }

    /**
     * 获取多个角色名称
     *
     * @param roles 角色代码数组
     * @return 角色名称字符串
     */
    private String getAnyRoleName(int[] roles) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < roles.length; i++) {
            sb.append(getRoleName(roles[i]));
            if (i < roles.length - 1) {
                sb.append("、");
            }
        }
        return sb.toString();
    }
}
