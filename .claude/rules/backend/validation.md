# 参数校验规范

## 校验责任划分

按调用链层级判定校验职责：

- **首轮调用**：Controller 方法，以及直接被 Controller 调用的 Service 方法，负责参数校验
- **非首轮调用**：仅被其他 Service 调用的 Service 方法（如内部工具方法、被同层服务复用的方法），禁止重复参数校验

边界判定：Service 方法同时被 Controller 和其他 Service 调用时，按首轮处理（存在未校验的入参路径）。

## 简单类型参数校验

类上添加 `@Validated`，方法简单类型入参直接使用校验注解（`@NotBlank`、`@NotNull`、`@Min` 等）：

```java
@Validated
@Service
public class UserService {

    public User getByUsername(@NotBlank String username) { ... }
}
```

## 模型类参数校验

模型类入参类型前添加 `@Valid`，校验规则由模型类字段的注解表达：

```java
public User createUser(@Valid UserCreateDTO dto) { ... }
```

## 禁止项

- 禁止用 `if` 语句做简单数据校验（null、空字符串、范围等），必须使用校验注解
