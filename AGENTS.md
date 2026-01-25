# ERP (Spring Boot) - Working Notes for Agents

This repository is a single-module Spring Boot application (`com.dat:erp`) built with Maven.

## Quick Commands (Windows / PowerShell)

- Build: `.\mvnw clean package`
- Run app: `.\mvnw spring-boot:run`
- Run tests: `.\mvnw test`
- Run one test class: `.\mvnw -Dtest=SomeTest test`
- Run with coverage gate (JaCoCo): `.\mvnw verify` (enforces minimum instruction coverage)

## Runtime Configuration

`src/main/resources/application.properties` imports a local `.env` file:

- `spring.config.import=optional:file:.env[.properties]`

Expected `.env` keys for local/dev (values are project-specific):

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (PostgreSQL)
- `JWT_SECRET`, `JWT_EXPIRATION_MS`
- `MAIL_USERNAME`, `MAIL_PASSWORD` (SMTP)
- Email retry knobs: `EMAIL_MAX_RETRY`, `EMAIL_DEFAULT_FROM`, `EMAIL_RETRY_ENABLED`, `EMAIL_RETRY_FIXED_DELAY_MS`

Tests use `src/test/resources/application.properties` (H2 in-memory DB; Swagger disabled).

## Project Layout (High Level)

Main code: `src/main/java/com/dat/erp/`

- `controllers/`: REST controllers (often annotated for Swagger/OpenAPI)
- `services/` + `services/impl/`: business logic + implementations
- `repositories/`: Spring Data JPA repositories
- `entities/`: JPA entities (some fields use converters for encryption/hashing)
- `dto/`: request/response models
- `mapper/`: MapStruct mappers
- `filters/`, `handlers/`, `exceptions/`: security/exception plumbing
- `constants/`: shared constants (e.g., role names in `Defaults`)

## Conventions

- Java: 17 (see `pom.xml`)
- Spring Boot: 3.5.4
- Formatting: VS Code is configured to use `eclipse-formatter.xml` (`.vscode/settings.json`); prefer using the formatter over manual reformatting.
- Lombok + MapStruct are used; keep changes consistent with existing patterns.

## Notes / Gotchas

- Coverage: JaCoCo is configured in `pom.xml`; `verify` generates a report and enforces a minimum coverage threshold.
- Security: the allowlist is configured via `security.whitelist` in properties; controllers may use method security annotations (e.g., `@PreAuthorize`).
- If you touch encryption-related code (e.g., `EncryptFieldConverter` / `CryptoUtils`), consider data compatibility/migrations.
- Current state: `.\mvnw test` fails in `SalaryTemplateServiceImplTest` due to a null `SecurityContextService` (test wiring/mocking issue).

