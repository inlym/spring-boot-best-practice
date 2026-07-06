# spring-boot-json

JSON 处理模块 —— 演示 Spring Boot 4 下控制器层面的 JSON 序列化与反序列化最佳实践。

## 模块定位

聚焦演示 **控制器层面** 的 JSON 序列化与反序列化，即 HTTP 请求体的自动反序列化（`@RequestBody`）与响应体的自动序列化效果，不涉及非
HTTP 场景下的手动 JSON 处理。

所有 Java 类型与 JSON 之间的转换规则集中在 [`JacksonConfig`](src/main/java/com/example/json/config/JacksonConfig.java)
一处维护，业务代码无需感知序列化细节。

## Jackson 全局配置（`JacksonConfig`）

通过 `JsonMapperBuilderCustomizer` 向 Spring Boot 自动配置的 `JsonMapper` 注入统一序列化策略，全模块生效，业务代码无需重复声明。

| 配置项                                                    | 设置  | 作用                | 采用原因                                           |
|--------------------------------------------------------|-----|-------------------|------------------------------------------------|
| `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES`    | 禁用  | 反序列化忽略 JSON 中未知字段 | API 字段新增/变更时不中断旧版本客户端的反序列化                     |
| `EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL`         | 启用  | 未知枚举值反序列化为 `null` | 第三方服务返回新枚举值时映射为 `null`，不中断反序列化                 |
| `SerializationFeature.FAIL_ON_EMPTY_BEANS`             | 禁用  | 空对象序列化不抛异常        | DTO 字段全为 `null` 被 `NON_NULL` 过滤后的兜底，避免序列化空对象报错 |
| `defaultTimeZone(UTC)`                                 | UTC | 时间戳基准时区           | 避免运行环境时区差异导致序列化结果不一致                           |
| `DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS`            | 启用  | 时间类型序列化为数字时间戳     | 覆盖默认 ISO-8601 字符串格式，统一前后端数据交换格式                |
| `DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS` | 禁用  | 序列化时间戳使用毫秒精度      | 匹配前端 `Number` 安全整数范围（详见下节）                     |
| `DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS`  | 禁用  | 反序列化时间戳按毫秒解析      | 与序列化端精度保持一致                                    |
| `JsonInclude.Include.NON_NULL`                         | 启用  | 响应体不输出 `null` 字段  | 减少无效传输                                         |

## 时间传递规范

### 约定

**前后端所有时间字段统一使用毫秒级时间戳（`long` 数字）传递**，不使用 ISO-8601 字符串、不使用纳秒精度。

- **请求体（前端 → 后端）**：时间字段传毫秒时间戳数字，后端 `@RequestBody` 自动反序列化为 `Instant`
- **响应体（后端 → 前端）**：`Instant` 自动序列化为毫秒时间戳数字

```jsonc
// 请求 / 响应中的时间字段形态
{
  "createTime": 1700000000000
}
```

### 实现原理

由 `DateTimeFeature` 三个开关组合实现，无需手写 `Serializer` / `Deserializer`：

- 开启 `WRITE_DATES_AS_TIMESTAMPS`：时间序列化为数字而非 ISO-8601 字符串
- 关闭 `WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS`：序列化精度从默认纳秒降为毫秒
- 关闭 `READ_DATE_TIMESTAMPS_AS_NANOSECONDS`：反序列化按毫秒解析，与序列化端对齐

配合 `defaultTimeZone(UTC)` 锁定基准时区，毫秒时间戳的语义在所有运行环境下完全一致。

### 为什么是毫秒时间戳

- **前端 `Number` 安全整数范围**：JavaScript 的 `Number` 安全整数上限为 2^53 − 1（约 9 × 10^15）。毫秒时间戳（如 1.7 ×
  10^12）远在安全范围内；而纳秒时间戳（1.7 × 10^18）会超出该范围，前端接收时精度丢失
- **为什么不用 ISO-8601 字符串**：字符串需要前后端各自解析/格式化，且时区表述（`Z`、`+08:00`
  等）容易引入歧义；毫秒时间戳是原生数字，运算与比较更直接，时区已由 UTC 基准统一隐含

## 演示端点

| 方法     | 路径              | 演示要点                                                 |
|--------|-----------------|------------------------------------------------------|
| `POST` | `/users`        | 回显请求体：未知字段忽略、Instant 毫秒时间戳双向转换、`null` 字段过滤           |
| `GET`  | `/users/sample` | 返回构造的示例对象，演示响应体自动序列化：Instant 毫秒时间戳、空字段 `NON_NULL` 过滤 |

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

## 快速启动

从项目根目录执行（`-pl` 指定本模块）：

```bash
mvn spring-boot:run -pl spring-boot-json
```

默认端口 `8080`。环境要求、端口覆盖、项目级启动方式详见[根 README](../README.md)。
