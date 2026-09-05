# AGENTS.md

Single-module Spring Boot 4.1.1 / Java 25 (toolchain) / Gradle 9.7.1 wrapper project. Package root: `id.my.agungdh.testcrudappapiv4`. Currently a scaffold — only the application class + `contextLoads` test exist.

## Commands

- Build jar (what CI runs): `./gradlew bootJar --no-daemon`
- Test: `./gradlew test --no-daemon`; single test: `./gradlew test --no-daemon --tests "id.my.agungdh.testcrudappapiv4.TestCrudAppApiV4ApplicationTests"`
- Run app: `./gradlew bootRun --no-daemon` (needs infra up first)
- Infra: `docker compose up -d postgres valkey minio` (all ports bound to `127.0.0.1`)
- Init MinIO bucket (after minio healthy, default bucket `testcrud`): `./gradlew initMinio`
- Reset postgres (DESTROYS DATA): `./gradlew recreatePostgres`
- UIs: Adminer `:8083`, redis-commander `:8084`, MinIO console `:9001`.

## DB / entity conventions (mandatory for every table)

Every table MUST follow this shape:

- `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY` — internal only, **never exposed to FE** (no JSON, no DTO, no URL param).
- `uuid UUID NOT NULL DEFAULT gen_random_uuid()` (UUID v4). Hash index, **non-unique** (Postgres HASH indexes can't be unique; v4 collision risk is negligible so this is accepted):
  `CREATE INDEX IF NOT EXISTS <table>_uuid_idx ON <table> USING HASH (uuid);`
- FKs stored as `<ref>_id BIGINT` internally, exposed to FE only as `<ref>_uuid UUID`. Service layer resolves incoming `<ref>_uuid` via `findByUuidAndDeletedAtIsNull`, never accepts `<ref>_id` from client.
- Audit columns on every table:
  `created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by BIGINT NULL,`
  `updated_at TIMESTAMPTZ NULL, updated_by BIGINT NULL,`
  `deleted_at TIMESTAMPTZ NULL, deleted_by BIGINT NULL`
  `*_by` holds the actor's internal `id` (plain `BIGINT NULL`, no FK constraint so history survives user deletes / system actions allowed). `*_at` is always `TIMESTAMPTZ`.
- Soft delete via `deleted_at`/`deleted_by`. All app queries filter `WHERE deleted_at IS NULL`.
- Any logical UNIQUE becomes a **partial unique index**, never a table-level `UNIQUE` constraint, e.g.:
  `CREATE UNIQUE INDEX IF NOT EXISTS <table>_<col>_uniq ON <table> (<col>) WHERE deleted_at IS NULL;`
  Same for composite uniques. Plain (non-unique) lookups on soft-deletable columns should also carry `WHERE deleted_at IS NULL` when the index supports it.

## DTO / API conventions

- All DTOs are Java `record`s.
- Entity ↔ DTO mapping via MapStruct `@Mapper` interfaces only (no manual mapping); `componentModel = "spring"`, ignore `id` / `<ref>_id`, map FK as e.g. `@Mapping(target = "authorUuid", source = "author.uuid")`.
- Request/response DTOs carry `uuid` / `<ref>_uuid` only. `id` / `<ref>_id` fields are forbidden in DTOs and JSON (MapStruct mappers must ignore them; use a lookup for FK uuids).

## Gotchas — read before coding

- `src/main/resources/db/migration/` does **not** exist yet, but `application.yaml` sets `flyway.enabled: true` + `jpa.hibernate.ddl-auto: validate`. App/test context **fails to start** until you add a Flyway migration for every entity. Always create `V<n>__*.sql` alongside new entities.
- `@SpringBootTest contextLoads` requires live Postgres (`127.0.0.1:5432`, db `crud`, user/pass `admin`/`admin`) AND Valkey/Redis (`127.0.0.1:6379`, password `admin`). Start compose before `./gradlew test`.
- Config is env-overridable: `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `SPRING_DATA_REDIS_HOST/PORT/PASSWORD`. Defaults already match `docker-compose.yml`.
- MinIO image is `pgsty/silo:latest` (S3-compatible), not official MinIO; `mcli` CLI is available inside the container (used by `initMinio`).
- MapStruct `1.6.3` + Lombok are annotation-processor wired — keep `@Mapper` interfaces + `annotationProcessor` deps intact; plain `javac` without Gradle will miss generated impls.
- Virtual threads enabled (`spring.threads.virtual.enabled=true`); `open-in-view: false` — don't rely on lazy loading in controllers.
- CI (`.github/workflows/build.yml`) builds the jar then runs `docker build` with `context: .`, but there is **no Dockerfile** in repo yet — don't assume container build works.
