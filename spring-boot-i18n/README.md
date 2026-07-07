# spring-boot-i18n

Spring Boot 4 下国际化（i18n）的最佳实践模块，聚焦演示 **基于 `Accept-Language` 请求头的多语言消息获取**——`MessageSource`
加载多语言资源文件、`LocaleResolver` 解析客户端语言、业务模块通过扩展接口声明各自资源文件。

## 模块架构

```
spring-boot-i18n/
├── pom.xml                       # 父 POM，聚合两个子模块
├── spring-boot-i18n-core/        # 核心配置（零业务耦合，可独立复制使用）
└── spring-boot-i18n-demo/        # 演示模块（HTTP 层多语言效果展示）
```

| 子模块                                              | 定位   | 说明                                                                                                    |
|--------------------------------------------------|------|-------------------------------------------------------------------------------------------------------|
| [`spring-boot-i18n-core`](spring-boot-i18n-core) | 核心配置 | 含 `I18nConfig`、`I18nService` 与 `MessageSourceBasenameCustomizer` 扩展接口，可原封不动（仅改包名）复制到任意 Spring Boot 项目 |
| [`spring-boot-i18n-demo`](spring-boot-i18n-demo) | 演示模块 | 完整 Spring Boot 应用，展示 core 配置 + 业务模块注册 basename 后的 HTTP 多语言效果                                          |

## 国际化配置

核心逻辑集中在 [`I18nConfig`](spring-boot-i18n-core/src/main/java/com/example/i18n/core/config/I18nConfig.java)
一处维护，声明两个 Bean：

| Bean             | 实现                            | 作用                                                |
|------------------|-------------------------------|---------------------------------------------------|
| `MessageSource`  | `ResourceBundleMessageSource` | 按 basename 加载 classpath 多语言资源文件，UTF-8 编码，关闭系统区域回退 |
| `LocaleResolver` | `AcceptHeaderLocaleResolver`  | 解析客户端 `Accept-Language` 请求头，白名单外的请求回退默认语言         |

- **支持语言**：中文（`zh-CN` 及任意 `zh-*`）、英文（`en-US` 及任意 `en-*`）。支持列表同时含区域限定条目与纯语言条目，裸语言标签（
  `zh`、`en`）及其他区域变体（`zh-TW`、`en-GB`）均可命中对应语言
- **默认语言**：`zh-CN`（请求未声明语言或不在白名单时兜底）
- **`MessageSource` 显式声明的原因**：Spring Boot 自动配置要求存在默认资源文件 `messages.properties`
  ，而本项目只保留语言特定的资源文件，故显式声明以替代自动配置

## 资源文件加载机制

### 核心模块资源

`I18nConfig` 直接注册核心模块 basename `i18n/core`，对应资源文件：

```
spring-boot-i18n-core/src/main/resources/i18n/core_zh.properties
spring-boot-i18n-core/src/main/resources/i18n/core_en.properties
```

### 业务模块资源（扩展接口）

业务模块通过实现 [
`MessageSourceBasenameCustomizer`](spring-boot-i18n-core/src/main/java/com/example/i18n/core/extension/MessageSourceBasenameCustomizer.java)
接口声明自身 basename，核心模块自动收集并注册：

```java

@Configuration
public class DemoMessageSourceBasenameCustomizer implements MessageSourceBasenameCustomizer {

    @Override
    public List<String> declareBasenames() {
        return List.of("i18n/demo");
    }
}
```

**basename 命名约定**：形如 `i18n/<模块标识>`，模块标识取业务模块包路径去掉 `com.example.` 前缀，将剩余段的点号替换为连字符（如
`com.example.account.user` → `i18n/account-user`）。

## 消息获取

[`I18nService`](spring-boot-i18n-core/src/main/java/com/example/i18n/core/service/I18nService.java) 封装消息获取，语言环境由请求上下文（
`LocaleContextHolder`）解析，对应客户端 `Accept-Language`：

```java
// 无参消息
String message = i18nService.getMessage("language.current");

// 带占位符参数的消息（资源文件中用 {0}、{1}... 表示占位符）
String welcome = i18nService.getMessage("greeting.welcome", new Object[]{name});
```

## 演示端点

| 方法    | 路径                   | 演示要点                                           |
|-------|----------------------|------------------------------------------------|
| `GET` | `/messages/language` | 核心资源消息：`Accept-Language` 切换语言，未声明时回退中文         |
| `GET` | `/messages/welcome`  | 演示模块资源消息（经定制器注册）：占位符 `{0}` 替换为 `name` 参数，随语言切换 |

> 调试以上端点可在 IntelliJ IDEA 中直接运行 [`i18n-demo.http`](spring-boot-i18n-demo/http/i18n-demo.http)，或使用下方
> curl 命令。

## 快速开始

从项目根目录执行：

```bash
mvn spring-boot:run -pl spring-boot-i18n/spring-boot-i18n-demo
```

验证：

```bash
# 获取当前语言（未发送 Accept-Language，回退默认中文）
curl http://localhost:8080/messages/language
# 预期响应：{"message":"简体中文"}

# 获取当前语言（英文请求头）
curl -H "Accept-Language: en" http://localhost:8080/messages/language
# 预期响应：{"message":"English"}

# 获取欢迎消息（占位符替换，默认中文）
curl 'http://localhost:8080/messages/welcome?name=Alice'
# 预期响应：{"message":"欢迎，Alice！"}

# 获取欢迎消息（英文请求头）
curl -H "Accept-Language: en" 'http://localhost:8080/messages/welcome?name=Alice'
# 预期响应：{"message":"Welcome, Alice!"}
```

## 复用 core 模块

1. **直接复制（推荐）**：将 `spring-boot-i18n-core` 中的 `I18nConfig.java`、`I18nService.java`、
   `MessageSourceBasenameCustomizer.java` 复制到目标项目，改 `package` 即可生效；同时复制 `i18n/core_*.properties` 资源文件
2. **Maven 依赖**：在目标项目 `pom.xml` 中声明 `spring-boot-i18n-core` 依赖，启动类扩大组件扫描范围至 core 包以装配
   `@Configuration` 与 `@Service`

> 推荐直接复制——core 模块不含业务逻辑，复制比引入依赖更轻量，且无版本耦合。
