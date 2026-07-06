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
- **core/demo 分离**：每个功能拆分为 `xxx-core`（纯配置，可独立复用）和 `xxx-demo`（演示集成用法）两个子模块，由功能父模块聚合

```
spring-boot-best-practice/
├── pom.xml                                # 根聚合 pom，公共依赖与插件管理
└── spring-boot-json/                      # JSON 处理父模块
    ├── pom.xml                            # 聚合 core 与 demo
    ├── spring-boot-json-core/             # 核心配置（可独立复制使用）
    └── spring-boot-json-demo/             # 演示模块（HTTP 层序列化效果演示）
```

## 模块清单

| 模块 | 说明 |
|------|------|
| `spring-boot-json` | JSON 处理父模块，聚合 core 与 demo |
| `spring-boot-json-core` | Jackson 全局配置，可原封不动（仅修改包名）复制到任意 Spring Boot 项目中使用 |
| `spring-boot-json-demo` | JSON 处理演示，展示 core 配置在 HTTP 层的序列化与反序列化效果 |

## 模块复用指南

### core 模块

`xxx-core` 模块设计为**零耦合的纯配置模块**，不依赖任何业务代码。复用方式：

1. **直接复制**：将 core 模块中的配置类复制到目标项目，仅修改 `package` 声明即可生效
2. **Maven 依赖**：在目标项目的 `pom.xml` 中声明对 core 模块的依赖

> 推荐直接复制方式——core 模块代码量极少（通常只有一个配置类），复制比引入依赖更轻量，且避免版本耦合。

### demo 模块

`xxx-demo` 模块演示如何集成 core 配置到完整的 Spring Boot 应用中，包括 Controller、DTO、测试用例和启动类。参考时关注：

- 启动类如何通过 `@Import` 引入 core 配置
- Controller 如何在不感知序列化细节的情况下获得正确的 JSON 行为
- 测试用例如何验证 HTTP 层的序列化/反序列化效果

### spring-boot-json-core

> 详细配置原理与复制使用方式见 [模块 README](spring-boot-json/README.md)。

仅包含一个 `JacksonConfig` 类，通过 `JsonMapperBuilderCustomizer` 注入反序列化容错（忽略未知字段、未知枚举映射为 null）、UTC 时区、`NON_NULL` 字段过滤、空 Bean 不抛异常，以及 Instant 毫秒时间戳的双向转换。

### spring-boot-json-demo

> 详细配置原理与时间传递规范见 [模块 README](spring-boot-json/README.md)。

展示 core 模块配置在 HTTP 层的完整效果。

演示端点：

| 方法 | 路径 | 演示要点 |
|------|------|----------|
| `POST` | `/users` | 回显请求体：未知字段忽略、Instant 毫秒时间戳双向转换、null 字段过滤 |
| `GET` | `/users/sample` | 返回构造的示例对象：Instant 毫秒时间戳、空字段 `NON_NULL` 过滤 |

## 快速开始

### 1. 环境要求

- JDK 25+
- Maven 3.9+

### 2. 启动模块

在项目根目录执行，`-pl` 指定要启动的模块：

```bash
# 启动 JSON 演示模块
mvn spring-boot:run -pl spring-boot-json/spring-boot-json-demo
```

默认端口 `8080`。如端口被占用，可通过参数覆盖：

```bash
mvn spring-boot:run -pl spring-boot-json/spring-boot-json-demo -Dspring-boot.run.arguments="--server.port=18080"
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
