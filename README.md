# spring-boot-best-practice

Spring Boot 4.x 各功能模块的最佳实践演示项目。每个模块独立可运行，聚焦演示一项功能的推荐用法与配置，配套最小示例代码。

架构借鉴 [javastacks/spring-boot-best-practice](https://github.com/javastacks/spring-boot-best-practice)。

## 技术栈

- **Spring Boot** 4.1.0 + **Java** 25
- **Jackson** 3（JSON 序列化，随 `spring-boot-starter-web` 提供）
- **Lombok**（样板代码消除）
- **Maven** 多模块 + `flatten-maven-plugin`（统一版本管理）

## 项目架构

采用多模块 Maven 架构，遵循以下原则：

- **根 pom 只保留公共依赖**：所有模块都会使用到的最小依赖集合（Web、Lombok、Test），专项依赖由各模块自行声明
- **每模块独立可启动**：每个模块包含独立的启动类，可单独编译、运行、验证
- **最小示例代码**：模块内只包含演示对应功能所必需的代码，不引入额外抽象

```
spring-boot-best-practice/
├── pom.xml                  # 根聚合 pom，公共依赖与插件管理
└── spring-boot-json/        # JSON 处理模块（控制器层面的序列化与反序列化）
```

## 模块清单

| 模块                 | 说明                         |
|--------------------|----------------------------|
| `spring-boot-json` | 演示控制器层面的 JSON 序列化与反序列化最佳实践 |

### spring-boot-json

> 详细配置原理与时间传递规范见 [模块 README](spring-boot-json/README.md)。

聚焦演示 Spring Boot 4 下**控制器层面**的 JSON 序列化与反序列化，即 HTTP 请求体的自动反序列化（`@RequestBody`）与响应体的自动序列化效果，不涉及非 HTTP 场景下的手动 JSON 处理。

演示要点：

- **全局 Jackson 配置**（`JacksonConfig`）：通过 `JsonMapperBuilderCustomizer` 注入反序列化容错（忽略未知字段、未知枚举映射为
  null）、UTC 时区、`NON_NULL` 字段过滤、空 Bean 不抛异常
- **Instant 毫秒时间戳**：通过 `DateTimeFeature` 三个开关（开启 `WRITE_DATES_AS_TIMESTAMPS`、关闭纳秒读写）实现 `Instant` 与毫秒时间戳的双向转换，无需手写 Serializer/Deserializer

演示端点：

| 方法     | 路径              | 演示要点                                        |
|--------|-----------------|---------------------------------------------|
| `POST` | `/users`        | 回显请求体：未知字段忽略、Instant 毫秒时间戳双向转换、null 字段过滤    |
| `GET`  | `/users/sample` | 返回构造的示例对象，演示响应体自动序列化：Instant 毫秒时间戳、空字段 `NON_NULL` 过滤 |

## 快速开始

### 1. 环境要求

- JDK 25+
- Maven 3.9+

### 2. 启动模块

在项目根目录执行，`-pl` 指定要启动的模块：

```bash
mvn spring-boot:run -pl spring-boot-json
```

默认端口 `8080`。如端口被占用，可通过参数覆盖：

```bash
mvn spring-boot:run -pl spring-boot-json -Dspring-boot.run.arguments="--server.port=18080"
```

### 3. 验证

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

## 项目约定

详细的代码规范、命名约定、工作流规范见 [.claude/rules/](.claude/rules/) 目录。
