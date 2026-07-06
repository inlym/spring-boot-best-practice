# 任务完成评审规范

任务实施完成后，对改动代码逐条执行以下检查与处理，全部通过方可视为完成。

## try...catch 评审

逐条评估改动代码中的 `try...catch` 语句：

- 不必要的 `try...catch` 必须移除，让异常向上传播由全局异常处理器统一捕获
- 必要的 `try...catch`（如 SDK 调用受检异常、fallback 场景）必须在 `try` 上方用 `//` 注释说明使用原因和不使用的后果

具体规则见 `backend/exception-handling.md`。

## 依赖关系检查

检查新增代码引入的依赖是否完整且正确：

- `import` 语句齐全，禁止未使用的导入，禁止通配符导入
- 跨模块调用时 Maven 依赖已在对应模块 `pom.xml` 中声明
- Bean 注入符合 `backend/spring-layering.md`（使用 `@RequiredArgsConstructor` + `private final`，禁止 `@Autowired`）

## 规范合规检查

对照 `.claude/rules/` 目录下所有规范，逐项核对新增代码。涉及多规范交叉时以更严格的约定为准。重点项：

- 命名（`naming/` 目录全部文件）
- 代码风格（`backend/java-code-style.md`）
- 异常处理（`backend/exception-handling.md`）
- 参数校验（`backend/validation.md`）
- 日志（`backend/logging.md`）

## 方法内部注释检查

方法内部每个逻辑步骤必须用 `//` 注释说明其业务含义（"是什么"或"为什么"），不描述代码行为。具体规则见 `backend/java-code-style.md` 的"方法内部注释"章节。

## 修改文件重新优化

对有改动的类文件按规范整体走查一次，覆盖命名、注释、代码风格、字段与方法分组等。发现不符合处直接优化，无需向用户确认。

重新优化引入的新代码需回到上述检查项再次走查，避免优化本身产生新的违规。

## 二次评审

完成上述检查后，对全部改动代码再做一次整体评审，主动提出可执行的改进建议（不限于已列出的检查项），交由用户决定是否实施。
