# spring-boot-redis

Spring Boot 4 下 Redis 的最佳实践模块，聚焦演示 **三类 `RedisTemplate` 的序列化策略与存取用法**——对象用 JSON 序列化（`@class`
保留类型）、具化对象用类型安全模板（无类型信息开销）、二进制用字节数组原生序列化。

## 模块架构

```
spring-boot-redis/
├── pom.xml                       # 父 POM，聚合两个子模块
├── spring-boot-redis-core/       # 核心配置（零业务耦合，可独立复制使用）
└── spring-boot-redis-demo/       # 演示模块（三类模板的存取效果展示）
```

| 子模块                                                 | 定位   | 说明                                                                                                              |
|-----------------------------------------------------|------|---------------------------------------------------------------------------------------------------------------|
| [`spring-boot-redis-core`](spring-boot-redis-core/) | 核心配置 | 含 `RedisTemplateConfig`、`RedisTemplateService` 与 Jackson 3 序列化器，可原封不动（仅改包名）复制到任意 Spring Boot 项目 |
| [`spring-boot-redis-demo`](spring-boot-redis-demo/) | 演示模块 | 完整 Spring Boot 应用，展示泛化对象、具化对象、二进制三类模板的存取效果                                                                    |

## RedisTemplate 配置

核心逻辑集中在 [`RedisTemplateConfig`](spring-boot-redis-core/src/main/java/com/example/redis/core/config/RedisTemplateConfig.java)
，声明两个 Bean：

| Bean                   | 值序列化器                                | 适用场景                                                  |
|------------------------|--------------------------------------|-------------------------------------------------------|
| `redisTemplate`        | `GenericJackson3JsonRedisSerializer` | 值类型不固定的对象存储，序列化结果含 `@class` 字段记录原始类型，反序列化时自动还原        |
| `redisTemplateBytes`   | `RedisSerializer.byteArray()`        | 图片、音频等二进制内容，字节原样存取，避免 JSON 编解码带来的开销与潜在损坏              |

- **键序列化**：两个模板的 key 与 hash key 均用字符串序列化，便于 `redis-cli` 直接查看
- **Jackson 3 序列化器**：Spring Data Redis 4.x 仅提供 Jackson 2.x 实现，本项目基于 Jackson 3 `JsonMapper` 重新实现等价的
  `GenericJackson3JsonRedisSerializer`（带类型信息）与 `Jackson3JsonRedisSerializer`（具化、无类型信息）

## 类型安全模板

[`RedisTemplateService`](spring-boot-redis-core/src/main/java/com/example/redis/core/service/RedisTemplateService.java)
为值类型已知的场景提供工厂方法，创建的模板按指定类型序列化，存储不含 `@class`：

```java
// 创建针对 UserInfoDTO 类型的 RedisTemplate，整个生命周期复用同一实例
RedisTemplate<String, UserInfoDTO> userTemplate = redisTemplateService.createRedisTemplate(UserInfoDTO.class);

userTemplate.opsForValue().set("user:" + id, user);
UserInfoDTO cached = userTemplate.opsForValue().get("user:" + id);
```

## 演示端点

| 方法     | 路径                          | 模板类型 | 演示要点                                                     |
|--------|-----------------------------|------|--------------------------------------------------------|
| `POST` | `/redis/objects/{key}`      | 泛化对象 | 存入对象，序列化结果含 `@class`                                    |
| `GET`  | `/redis/objects/{key}`      | 泛化对象 | 取出对象，`@class` 还原为原始类型                                   |
| `POST` | `/redis/users/{key}`        | 具化对象 | 存入对象，序列化结果不含 `@class`，体积更小                              |
| `GET`  | `/redis/users/{key}`        | 具化对象 | 取出对象，直接返回 `UserInfoDTO` 类型                              |
| `POST` | `/redis/binaries/{key}`     | 二进制  | 存入字节数组，JSON 中以 Base64 传输                                |
| `GET`  | `/redis/binaries/{key}`     | 二进制  | 取出原始字节，JSON 中以 Base64 返回                                |

> 调试以上端点可在 IntelliJ IDEA 中直接运行 [`redis-demo.http`](spring-boot-redis-demo/http/redis-demo.http)，或使用下方 curl 命令。

## 快速开始

前置条件：本地或可访问的 Redis 实例（默认 `localhost:6379`）。

从项目根目录执行：

```bash
mvn spring-boot:run -pl spring-boot-redis/spring-boot-redis-demo
```

验证：

```bash
# 存入对象（泛化模板，序列化结果含 @class）
curl -X POST http://localhost:8080/redis/objects/user:1001 \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","age":28,"createTime":1751800000000,"remark":"泛化模板演示"}'
# 预期响应：{"username":"alice","age":28,"createTime":1751800000000,"remark":"泛化模板演示"}

# 取出对象（@class 还原为原始类型）
curl http://localhost:8080/redis/objects/user:1001
# 预期响应：{"username":"alice","age":28,"createTime":1751800000000,"remark":"泛化模板演示"}

# 存入二进制（Base64 编码 "hello redis" 的 UTF-8 字节）
curl -X POST http://localhost:8080/redis/binaries/blob:3001 \
  -H "Content-Type: application/json" \
  -d '{"data":"aGVsbG8gcmVkaXM="}'
# 预期响应：{"data":"aGVsbG8gcmVkaXM="}

# 取出二进制
curl http://localhost:8080/redis/binaries/blob:3001
# 预期响应：{"data":"aGVsbG8gcmVkaXM="}
```

## 复用 core 模块

1. **直接复制（推荐）**：将 `spring-boot-redis-core` 中的 `RedisTemplateConfig.java`、`RedisTemplateService.java`、
   `GenericJackson3JsonRedisSerializer.java`、`Jackson3JsonRedisSerializer.java` 复制到目标项目，改 `package` 即可生效
2. **Maven 依赖**：在目标项目 `pom.xml` 中声明 `spring-boot-redis-core` 依赖，启动类扩大组件扫描范围至 core 包以装配
   `@Configuration` 与 `@Service`

> 推荐直接复制——core 模块不含业务逻辑，复制比引入依赖更轻量，且无版本耦合。
