---
trigger: always_on
---
## 开发规范

#### 包命名规范
- 后端包结构: `com.swimmingsys.{module}.{layer}`
  - 例如: `com.swimmingsys.member.controller`、`com.swimmingsys.course.service`
- 前端组件命名: 采用大驼峰命名法，如 `MemberInfo.vue`、`CourseList.vue`

#### 类/方法命名规范
- 类名使用大驼峰命名法 (UpperCamelCase)
- 方法名使用小驼峰命名法 (lowerCamelCase)
- 常量全部大写，单词间用下划线分隔(比如USER_NAME)
- 接口实现类使用 `Impl` 后缀

#### Java代码规范
- 使用空格缩进（4个空格），禁止使用Tab
- 类和方法之间用单行空行分隔
- 注释使用中文，重要注释使用JavaDoc格式

#### 数据库表命名规范
- 表名全部小写，单词间用下划线分隔
- 主键统一使用 `id`
- 时间字段使用 `created_time`、`updated_time` 格式
- 有关用户角色的表中必须有is_delete逻辑删除字段
- 外键字段使用 `{table}_id` 格式

### 数据库设计规范

#### 字段类型规范
- 主键使用BIGINT类型
- 文本字段优先使用VARCHAR，长度根据实际需要设定
- 日期时间使用DATETIME类型
- 状态字段使用TINYINT类型
- 数值字段根据范围选择合适类型

#### 索引规范
- 主键自动创建索引
- 外键字段建立索引
- 经常用于查询的字段建立索引
- 避免过多索引影响写入性能

### 个人喜好规范
-- 操作数据库时，使用QueryWrapper类来写需要查询的sql语句
--  Controller 不直接访问数据库，必须走 Service
-- 实体类用@Data(引入lombok依赖)注解简化多余代码
-- 不要使用@Autowired注解，改为用@Resource注解进行依赖注入
1  controller层对传回的参数要做非空判断
2
3
1
2
3


#### Vue代码规范
- 使用Composition API
- 组件props定义使用具体类型和默认值
- 使用ESLint和Prettier进行代码格式化
- 组件内部按模板、脚本、样式顺序组织
- 


### API设计规范

#### RESTful API设计
- GET /api/v1/members - 获取会员列表
- POST /api/v1/members - 创建会员
- GET /api/v1/members/{id} - 获取指定会员
- PUT /api/v1/members/{id} - 更新指定会员
- DELETE /api/v1/members/{id} - 删除指定会员

#### 返回格式规范
```
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

#### 错误码规范
- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 权限不足
- 404: 资源不存在
- 500: 服务器内部错误

### 安全规范

#### 密码安全
- 会员密码必须经过MD5加密后再存储
- 推荐使用MD5进行密码加密
- 禁止明文存储密码敏感信息

#### 权限控制
- 所有接口都需要进行权限验证
- 使用JWT进行身份认证
- 不同角色访问不同的功能模块
- 对关键操作进行二次验证

#### SQL注入防护
- 使用MyBatis Plus的预编译语句
- 禁止拼接SQL字符串
- 对用户输入进行严格校验

### 测试规范

#### 单元测试
- 业务逻辑方法必须编写单元测试
- 测试覆盖率不低于80%
- 使用JUnit 5进行测试

#### 接口测试
- 所有API接口需要进行功能测试
- 使用Swagger进行接口测试
- 验证正常和异常情况下的返回结果

### 部署规范

#### 环境配置
- 开发环境: application-dev.yml
- 测试环境: application-test.yml
- 生产环境: application-prod.yml

### 开发注意事项
- 在进行任何开发前，确保理解业务逻辑和用户角色关系
- 所有接口都要进行权限校验，防止未授权访问
- 数据库操作必须考虑事务处理，确保数据一致性
- 对于并发操作（如课程预约），要考虑并发安全问题
- 前端表单提交前进行必要校验，后端再次校验
- 合理使用缓存，提高系统性能
- 记录必要的操作日志，便于问题追踪和调试
