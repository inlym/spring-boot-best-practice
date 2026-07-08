# spring-boot-mybatisflex

Spring Boot 4 下 MyBatis-Flex ORM 框架的最佳实践模块，聚焦演示 **控制器层面** 通过 MyBatis-Flex 对 MySQL/H2 进行增删改查等基础操作——逻辑删除、SQL 审计日志、实体映射、`IService` / `BaseMapper` 的典型用法。

## 模块架构

```
spring-boot-mybatisflex/
├── pom.xml                                # 父 POM，聚合两个子模块
├── spring-boot-mybatisflex-core/          # 核心配置（零耦合，可独立复制使用）
└── spring-boot-mybatisflex-demo/          # 演示模块（H2 内存数据库集成演示）
```

| 子模块                                                                 | 定位   | 说明                                                         |
|---------------------------------------------------------------------|------|------------------------------------------------------------|
| [`spring-boot-mybatisflex-core`](spring-boot-mybatisflex-core/)     | 核心配置 | 仅含 `MyBatisFlexConfig` + `MyBatisFlexLogMessageCollector`，可原封不动（仅改包名）复制到任意 Spring Boot 项目 |
| [`spring-boot-mybatisflex-demo`](spring-boot-mybatisflex-demo/)     | 演示模块 | 完整 Spring Boot 应用，使用 H2 内存数据库展示 core 配置的实际效果                    |

启动类通过 `@Import(MyBatisFlexConfig.class)` 显式引入 core 配置，Controller 和 Service 无需感知 ORM 配置细节。

## MyBatis-Flex 全局配置

所有 ORM 配置规则集中在 [`MyBatisFlexConfig`](spring-boot-mybatisflex-core/src/main/java/com/example/mybatisflex/core/config/MyBatisFlexConfig.java) 一处维护：

| 配置项            | 作用                              | 采用原因                                     |
|----------------|---------------------------------|------------------------------------------|
| 逻辑删除处理器        | 使用 `DateTimeLogicDeleteProcessor` | 逻辑删除时自动填充 `delete_time` 为当前时间戳，查询自动过滤已删除数据 |
| SQL 审计         | 启用 `AuditManager`              | 收集所有 SQL 执行信息，记录执行耗时和完整 SQL 语句           |
| 自定义日志收集器       | `MyBatisFlexLogMessageCollector` | SQL 日志中换行符替换为 `↩︎` 符号，保持单行输出，便于日志检索        |
| Banner 关闭      | `config.setPrintBanner(false)`   | 关闭启动时控制台 Banner 打印，减少日志噪音                 |

### SQL 审计日志格式

```
[SQL] [2ms] [返回1行] SELECT * FROM `user_info` WHERE `username` = ?  AND `delete_time` IS NULL
[SQL] [0ms] [插入1行] INSERT INTO `user_info`(`username`, `email`, `age`) VALUES (?, ?, ?)
[SQL] [1ms] [更新1行] UPDATE `user_info` SET `email` = ?, `age` = ? WHERE `id` = ?
[SQL] [0ms] [删除1行] UPDATE `user_info` SET `delete_time` = ? WHERE `id` = ? AND `delete_time` IS NULL
```

> 逻辑删除本质上是 UPDATE 操作（填充 `delete_time`），而非物理 DELETE。所有 SELECT 查询自动追加 `AND delete_time IS NULL` 条件。

## 演示端点

| 方法       | 路径                    | 演示要点                                                     |
|----------|-----------------------|----------------------------------------------------------|
| `POST`   | `/users`              | 新增用户：`save()` 内部调用 `insertSelective`，仅插入非 null 字段，数据库默认值生效 |
| `GET`    | `/users/{userId}`     | 主键查询：`getById()` 自动过滤已逻辑删除数据                              |
| `GET`    | `/users/by-username`  | 条件查询：演示 `QueryWrapper` + Lambda 表达式构建查询条件                   |
| `PUT`    | `/users/{userId}`     | Builder 方式更新：仅传需更新的字段（邮箱、年龄），忽略 null 字段                   |
| `DELETE` | `/users/{userId}`     | 逻辑删除：`removeById()` 自动填充 `delete_time`，数据不物理删除             |

## 关键代码示例

### 实体类

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_info")
public class User {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Instant createTime;
    private Instant updateTime;
    @Column(isLogicDelete = true)
    private Instant deleteTime;
    // 业务字段...
}
```

### 自定义查询

```java
public User findByUsername(String username) {
    QueryWrapper queryWrapper = QueryWrapper.create()
        .select()
        .where(User::getUsername).eq(username);
    return getOne(queryWrapper);  // getOne 无结果时返回 null
}
```

### Builder 方式更新

```java
User updateUser = User.builder()
    .id(userId)
    .email(newEmail)
    .age(newAge)
    .build();
userService.updateById(updateUser);  // 按主键更新，忽略 null 字段
```

## 快速开始

从项目根目录执行：

```bash
mvn spring-boot:run -pl spring-boot-mybatisflex/spring-boot-mybatisflex-demo
```

验证：

```bash
# 创建用户
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","age":25}'
# 预期响应：{"id":1,"username":"alice","email":"alice@example.com","age":25}

# 按主键查询
curl http://localhost:8080/users/1

# 按用户名查询
curl "http://localhost:8080/users/by-username?username=alice"

# 更新用户
curl -X PUT http://localhost:8080/users/1 \
  -H "Content-Type: application/json" \
  -d '{"email":"alice.new@example.com","age":26}'

# 逻辑删除
curl -X DELETE http://localhost:8080/users/1
# 预期响应：{"success":true}
```

> 本模块使用 **H2 内存数据库**，零环境依赖，启动即用。每次应用重启后表结构由 `schema.sql` 自动重建，数据不持久化。

## 复用 core 模块

1. **直接复制（推荐）**：将 `spring-boot-mybatisflex-core` 中的 `MyBatisFlexConfig.java` 和 `MyBatisFlexLogMessageCollector.java` 复制到目标项目的对应包，改 `package` 即可生效
2. **Maven 依赖**：在目标项目 `pom.xml` 中声明 `spring-boot-mybatisflex-core` 依赖

> 推荐直接复制——core 模块只有两个类，复制比引入依赖更轻量，且无版本耦合。
