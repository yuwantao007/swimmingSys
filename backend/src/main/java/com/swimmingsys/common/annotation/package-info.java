/**
 * 权限控制注解包
 * 
 * <h2>使用示例：</h2>
 * 
 * <h3>1. 只允许管理员访问</h3>
 * <pre>
 * {@code
 * @AuthCheck(mustRole = RoleConstant.ADMIN)
 * @DeleteMapping("/{id}")
 * public Result<Void> deleteUser(@PathVariable Long id) {
 *     // 只有管理员（role=0）可以访问
 * }
 * }
 * </pre>
 * 
 * <h3>2. 允许多个角色访问（满足任意一个即可）</h3>
 * <pre>
 * {@code
 * @AuthCheck(anyRole = {RoleConstant.ADMIN, RoleConstant.MEMBER})
 * @GetMapping("/courses")
 * public Result<List<Course>> getCourses() {
 *     // 管理员和会员都可以访问
 * }
 * }
 * </pre>
 * 
 * <h3>3. 所有登录用户都可以访问</h3>
 * <pre>
 * {@code
 * @AuthCheck(anyRole = {RoleConstant.ADMIN, RoleConstant.MEMBER, RoleConstant.NON_MEMBER})
 * @PutMapping("/profile")
 * public Result<UserVO> updateProfile(@RequestBody UserUpdateDTO dto) {
 *     // 所有登录用户都可以访问
 * }
 * }
 * </pre>
 * 
 * <h2>角色常量：</h2>
 * <ul>
 *   <li>RoleConstant.ADMIN = 0（管理员）</li>
 *   <li>RoleConstant.MEMBER = 1（会员）</li>
 *   <li>RoleConstant.NON_MEMBER = 2（非会员）</li>
 * </ul>
 * 
 * @author swimmingsys
 */
package com.swimmingsys.common.annotation;
