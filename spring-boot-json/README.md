# spring-boot-json

Spring Boot 4 下 Jackson JSON 序列化与反序列化的最佳实践模块，聚焦演示 **控制器层面** 的 JSON 处理——HTTP 请求体的自动反序列化（`@RequestBody`）与响应体的自动序列化效果。

## 模块架构

```
spring-boot-json/
├── pom.xml                       # 父 POM，聚合两个子模块
├── spring-boot-json-core/        # 核心配置（零耦合，可独立复制使用）
└── spring-boot-json-demo/        # 演示模块（HTTP 层序列化效果展示）
```

| 子模块                                               | 定位   | 说明                                                 |
|---------------------------------------------------|------|----------------------------------------------------|
| [`spring-boot-json-core`](spring-boot-json-core/) | 核心配置 | 仅含 `JacksonConfig`，可原封不动（仅改包名）复制到任意 Spring Boot 项目 |
| [`spring-boot-json-demo`](spring-boot-json-demo/) | 演示模块 | 完整 Spring Boot 应用，展示 core 配置在 HTTP 层的实际效果          |

启动类通过 `@Import(JacksonConfig.class)` 显式引入 core 配置，Controller 和 DTO 无需感知序列化细节。

## Jackson 全局配置

所有 JSON 转换规则集中在 [`JacksonConfig`](spring-boot-json-core/src/main/java/com/example/json/core/config/JacksonConfig.java) 一处维护：

| 配置项                                    | 设置  | 作用                | 采用原因                                |
|----------------------------------------|-----|-------------------|-------------------------------------|
| `FAIL_ON_UNKNOWN_PROPERTIES`           | 禁用  | 反序列化忽略未知字段        | API 字段变更时不中断旧版本客户端                  |
| `READ_UNKNOWN_ENUM_VALUES_AS_NULL`     | 启用  | 未知枚举值反序列化为 `null` | 第三方服务返回新枚举值时映射为 `null`              |
| `FAIL_ON_EMPTY_BEANS`                  | 禁用  | 空对象序列化不抛异常        | DTO 字段全为 `null` 被 `NON_NULL` 过滤后的兜底 |
| `defaultTimeZone(UTC)`                 | UTC | 时间戳基准时区           | 避免运行环境时区差异导致序列化结果不一致                |
| `WRITE_DATES_AS_TIMESTAMPS`            | 启用  | 时间类型序列化为数字时间戳     | 统一前后端数据交换格式                         |
| `WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS` | 禁用  | 序列化时间戳使用毫秒精度      | 匹配前端 `Number` 安全整数范围（详见下节）          |
| `READ_DATE_TIMESTAMPS_AS_NANOSECONDS`  | 禁用  | 反序列化时间戳按毫秒解析      | 与序列化端精度保持一致                         |
| `NON_NULL`                             | 启用  | 响应体不输出 `null` 字段  | 减少无效传输                              |

## 时间传递规范

### 约定

**前后端所有时间字段统一使用毫秒级时间戳（`long` 数字）传递**，不使用 ISO-8601 字符串、不使用纳秒精度。

- **请求体（前端 → 后端）**：时间字段传毫秒时间戳数字，后端 `@RequestBody` 自动反序列化为 `Instant`
- **响应体（后端 → 前端）**：`Instant` 自动序列化为毫秒时间戳数字

### 实现原理

由 `DateTimeFeature` 三个开关组合实现，无需手写 `Serializer`/`Deserializer`：

- 开启 `WRITE_DATES_AS_TIMESTAMPS`：时间序列化为数字而非 ISO-8601 字符串
- 关闭 `WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS`：序列化精度从纳秒降为毫秒
- 关闭 `READ_DATE_TIMESTAMPS_AS_NANOSECONDS`：反序列化按毫秒解析

配合 `defaultTimeZone(UTC)` 锁定基准时区，毫秒时间戳的语义在所有运行环境下完全一致。

### 为什么是毫秒时间戳

- **前端 `Number` 安全整数范围**：JavaScript 的 `Number` 安全整数上限为 2^53 − 1（约 9 × 10^15）。毫秒时间戳（如 1.7 × 10^12）远在安全范围内；纳秒时间戳（1.7 × 10^18）会超出该范围导致前端精度丢失
- **为什么不用 ISO-8601 字符串**：字符串需要前后端各自解析/格式化，且时区表述（`Z`、`+08:00` 等）容易引入歧义；毫秒时间戳是原生数字，运算与比较更直接

## 演示端点

| 方法     | 路径              | 演示要点                                       |
|--------|-----------------|--------------------------------------------|
| `POST` | `/users`        | 回显请求体：未知字段忽略、Instant 毫秒时间戳双向转换、`null` 字段过滤 |
| `GET`  | `/users/sample` | 返回构造的示例对象：Instant 毫秒时间戳、空字段 `NON_NULL` 过滤  |

## 快速开始

从项目根目录执行：

```bash
mvn spring-boot:run -pl spring-boot-json/spring-boot-json-demo
```

验证：

```bash
# 回显请求体（含未知字段 extra，预期被忽略）
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","age":28,"createTime":1700000000000,"extra":"ignored"}'
# 预期响应：{"username":"alice","age":28,"createTime":1700000000000}

# 获取示例用户（createTime 序列化为毫秒时间戳，remark 留空被过滤）
curl http://localhost:8080/users/sample
# 预期响应：{"username":"alice","age":28,"createTime":<毫秒时间戳>}
```

## 复用 core 模块

1. **直接复制（推荐）**：将 `spring-boot-json-core` 中的 `JacksonConfig.java` 复制到目标项目的 `config` 包，改 `package` 即可生效
2. **Maven 依赖**：在目标项目 `pom.xml` 中声明 `spring-boot-json-core` 依赖

> 推荐直接复制——core 模块只有一个配置类，复制比引入依赖更轻量，且无版本耦合。
