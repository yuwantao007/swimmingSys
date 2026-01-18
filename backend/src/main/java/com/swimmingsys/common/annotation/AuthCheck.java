package com.swimmingsys.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 用于标识需要进行角色权限校验的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须拥有的角色（精确匹配）
     * 0-管理员，1-会员，2-非会员
     */
    int mustRole() default -1;

    /**
     * 允许的角色列表（满足任意一个即可）
     * 0-管理员，1-会员，2-非会员
     */
    int[] anyRole() default {};
}
