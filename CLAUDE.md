# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Working Principles

- **Clarify before acting**: When motivation or goals are unclear, stop and discuss — don't proceed on assumptions.
- **Suggest the shortest path**: When the goal is clear but the approach isn't optimal, point it out directly and recommend a better way.
- **Trace to root cause**: Don't patch symptoms. Every decision must be able to answer "why."
- **Be concise**: Output should be to the point — cut anything that doesn't change the decision.

## Build & Run Commands

```bash
# Build the entire project (skip tests with -DskipTests)
mvn compile

# Run all tests
mvn test

# Run tests for a single module
mvn test -pl spring-boot-json

# Start a specific demo module (runs on port 8080 by default)
mvn spring-boot:run -pl spring-boot-json/spring-boot-json-demo

# Start on a different port
mvn spring-boot:run -pl spring-boot-json/spring-boot-json-demo \
  -Dspring-boot.run.arguments="--server.port=18080"
```

Maven wrapper (`mvnw`) is not present — use the system-installed `mvn`.

## Project Architecture

**Spring Boot 4.1.0 + Java 25** multi-module Maven project. The root `pom.xml` declares only dependencies common to all modules (spring-boot-starter-web, Lombok, spring-boot-starter-test, spring-boot-starter-webmvc-test). Each module declares its own specialized dependencies.

### Module Structure: core/demo Split

Every feature follows a three-tier module hierarchy:

```
spring-boot-xxx/                    # Parent POM (pom packaging), aggregates core + demo
├── spring-boot-xxx-core/           # Pure configuration — zero business logic, self-contained, copyable
└── spring-boot-xxx-demo/           # Runnable app showing integration (Controllers, tests, http/ files)
```

**core modules** are designed for direct reuse: copy the config class to any project and change the `package` declaration. They contain no business logic — only `@Configuration` classes, extension point interfaces, and shared utilities.

**demo modules** are runnable Spring Boot applications that demonstrate how to integrate the core config. Each has its own `main()` method, `application.yml`, Controllers, and tests.

### Extension Point Pattern

Core modules that need business-level customization define a **single-method interface** in an `extension` sub-package. The core config collects all implementations via Spring's `List<Interface>` injection and calls them at initialization time.

```
core/
└── extension/
    └── XxxCustomizer.java          # Interface with one customize/declare method

demo/
└── config/
    └── DemoXxxCustomizer.java      # @Component implementing the interface
```

Core config classes use `@RequiredArgsConstructor` + `private final List<XxxCustomizer> customizers` to collect implementations.

### Demo Startup Class Pattern

Demo applications assemble core and demo packages in one of two ways:

1. **`@Import`** — when core only has `@Configuration` classes outside the demo's component scan range (e.g., `JacksonConfig` from `com.example.json` imported into `com.example.redis` demo)
2. **`scanBasePackages`** — when core has `@Service`/`@Component` beans that can't be imported individually; the demo widens its scan to `com.example.xxx`, covering both core and demo packages

### Module Dependencies Between Features

Modules can depend on each other. For example, `spring-boot-redis-demo` depends on `spring-boot-json-core` and uses `@Import(JacksonConfig.class)` so the same Jackson customization (Instant millisecond timestamps, NON_NULL filtering) applies to Redis JSON serialization.

## Code Conventions

All coding conventions live in `.claude/rules/`. Key structural norms:

| Rule File                       | Domain                                                                              |
|---------------------------------|-------------------------------------------------------------------------------------|
| `naming/java-naming.md`         | Class, method, variable, package naming                                             |
| `naming/entity-javadoc.md`      | Entity class JavaDoc + database field definitions                                   |
| `naming/model-javadoc.md`       | Non-entity model class JavaDoc                                                      |
| `backend/java-code-style.md`    | Lombok, method ordering, formatting, line wrapping, internal comments               |
| `backend/spring-layering.md`    | Controller/Service/Config layering, DI, query method naming (`getBy*` vs `findBy*`) |
| `backend/validation.md`         | `@Validated` + annotation-based validation, no manual null checks                   |
| `backend/exception-handling.md` | No try-catch in controllers/services; global exception handler                      |
| `backend/logging.md`            | Slf4j, `@LogExecution`, level guidelines                                            |
| `backend/config-format.md`      | YAML grouping, comments, spacing                                                    |
| `api/response.md`               | Error codes independent of HTTP status codes                                        |
| `database/mysql.md`             | Table schema conventions, MyBatis-Flex usage                                        |
| `workflow/task-execution.md`    | **Evaluate first, get confirmation, then code**                                     |

### Key Rules to Internalize

- **No `var`**, no wildcard imports, no `record`, no `is` prefix on booleans
- **No `@Autowired`** — use `@RequiredArgsConstructor` + `private final`
- **No `try...catch`** in controllers/services except for checked exceptions from third-party SDKs (must comment why)
- **No `@Value`** — use `@ConfigurationProperties`
- **`getBy*` never returns null**, `findBy*` may return null
- **String emptiness check**: `StringUtils.hasText()`, never manual null + isBlank
- **Task workflow**: always evaluate and get user confirmation before writing code

## Git Constraints

**Do not** commit, push, create branches, or modify git history without explicit user authorization. You may inspect status and diffs freely. See `.claude/rules/git/git-constraints.md`.
