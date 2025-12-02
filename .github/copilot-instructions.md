# Copilot Instructions for Demo Project

## Project Overview

This is a **Spring Boot 3.3.5 + Kotlin 2.2.21** REST API with MySQL database integration. The project demonstrates a basic user management system with JPA/Hibernate for ORM.

- **Language**: Kotlin (JVM target: Java 17)
- **Build Tool**: Gradle 8.x
- **Framework**: Spring Boot 3.3.5 (NOT 4.0.0 - see build.gradle comments)
- **Database**: MySQL with Hibernate auto-DDL (`ddl-auto: update`)

## Architecture

### Component Structure
```
src/main/kotlin/com/example/demo/
├── DemoApplication.kt          # Spring Boot entry point
├── HelloController.kt           # REST endpoint example (@RestController)
├── config/InitConfig.kt         # Initialization & data seeding (CommandLineRunner bean)
└── user/                        # Domain: User management
    ├── UserEntity.kt            # JPA entity (jakarta.persistence.*)
    └── UserRepository.kt        # Spring Data JPA repository
```

### Data Flow
1. **Requests** → `HelloController` (REST endpoint)
2. **Database Access** → `UserRepository` extends `JpaRepository<UserEntity, Long>`
3. **Initialization** → `InitConfig` beans run at startup (CommandLineRunner)
4. **Configuration** → `application.yml` defines datasource & JPA settings

## Key Patterns & Conventions

### 1. **Kotlin + Spring Boot Integration**
- Use `data class` for entities (see `UserEntity.kt`)
- All Spring Boot starter classes use `@SpringBootApplication`
- Main function uses `runApplication<DemoApplication>(*args)` idiom
- Kotlin compiler settings: JVM target 17, `-Xjsr305=strict` enabled

### 2. **JPA Entity & Repository Pattern**
- Entities use `jakarta.persistence.*` (not `javax.persistence.*`) - Jakarta EE 10 standard
- `@Entity` on data classes with `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
- Repository extends `JpaRepository<T, ID>` - no custom implementation needed unless complex queries
- Example: `UserRepository.findByEmail(email: String)` is auto-implemented by Spring Data

### 3. **MySQL Configuration** (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/matchbell?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update  # Auto-creates/updates tables - OK for dev, use Flyway for production
    show-sql: true      # Logs SQL for debugging
```

### 4. **Initialization Pattern**
- Use `@Configuration` + `@Bean` returning `CommandLineRunner` for startup logic
- Example in `InitConfig.kt`: checks if table is empty, seeds test user
- Runs AFTER Spring context is fully initialized

### 5. **Controller Pattern**
- Simple `@RestController` classes with `@GetMapping` / `@PostMapping`
- Example: `HelloController.hello()` returns String (Spring serializes to JSON)

## Build & Test Workflows

### Build
```powershell
./gradlew clean build               # Full build with tests
./gradlew build -x test             # Build without running tests
./gradlew compileKotlin             # Kotlin compilation only
```

### Run
```powershell
./gradlew bootRun                   # Start Spring Boot server (default: http://localhost:8080)
```

### Test
```powershell
./gradlew test                      # Run all JUnit tests
```

### Common Issues
- **Build fails with Kotlin errors**: Verify `jvmTarget = "17"` in build.gradle
- **MySQL connection errors**: Ensure MySQL server running at `localhost:3306/matchbell`
- **Tables not created**: Check `application.yml` has `ddl-auto: update`

## Developer Conventions

1. **Package Structure**: Follow `com.example.demo.{feature}` pattern (e.g., `demo.user`, `demo.config`)
2. **Import Statements**: Use fully-qualified imports; avoid wildcard imports
3. **String Interpolation**: Use Kotlin `"value: $variable"` style in log messages
4. **Null Safety**: Leverage Kotlin nullability - repository queries return `T?` for optional results
5. **Entity Naming**: Use `{Domain}Entity.kt` and `{Domain}Repository.kt` naming
6. **SQL Logging**: `show-sql: true` in dev config - turn off in production

## Integration Points

- **MySQL Database**: Version required; credentials in `application.yml`
- **Spring Data JPA**: Automatic repository implementation - no custom SQL needed for basic CRUD
- **Kotlin Reflection**: Jackson module handles JSON serialization of Kotlin data classes
- **No Validation Library Yet**: Project commented out `spring-boot-starter-validation` - add if needed for @Valid/@NotNull annotations

## Future Migration Notes

- **DDL Management**: Currently using Hibernate auto-DDL (`update`); migrate to Flyway for production
- **Spring Boot Version**: Lock at 3.3.x - DO NOT upgrade to 4.0.0 (see build.gradle warning)
- **Redis Support**: HELP.md mentions Redis is a dependency but not yet used - integration code needs implementation

