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

Every entity MUST extend `common.BaseEntity` (a `@MappedSuperclass` carrying
`id`, `uuid`, `createdAt/createdBy`, `updatedAt/updatedBy`,
`deletedAt/deletedBy`). Entities must NOT redeclare any of those fields.
`BaseEntity` also carries `@SQLRestriction("deleted_at IS NULL")` so every
JPQL/Criteria query is auto-filtered to live rows; each entity re-declares the
same `@SQLRestriction` explicitly (inheritance-safe belt and suspenders).
MapStruct mappers must ignore `id` and all audit fields.

Every table MUST follow this shape (migration SQL still declares the base
columns explicitly, 1:1 with the `BaseEntity` mapping):

- `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY` — internal only, **never exposed to FE** (no JSON, no DTO, no URL param).
- `uuid UUID NOT NULL DEFAULT gen_random_uuid()` (UUID v4). Hash index, **non-unique** (Postgres HASH indexes can't be unique; v4 collision risk is negligible so this is accepted):
  `CREATE INDEX IF NOT EXISTS <table>_uuid_idx ON <table> USING HASH (uuid);`
- FKs stored as `<ref>_id BIGINT` internally, exposed to FE only as `<ref>_uuid UUID`. Service layer resolves incoming `<ref>_uuid` via `findByUuid` (soft-delete filter is automatic, no `AndDeletedAtIsNull` suffix needed), never accepts `<ref>_id` from client.
- Audit columns on every table:
  `created_at TIMESTAMPTZ NULL DEFAULT now(), created_by BIGINT NULL,`
  `updated_at TIMESTAMPTZ NULL DEFAULT now(), updated_by BIGINT NULL,`
  `*_by` holds the actor's internal `id` (plain `BIGINT NULL`, no FK constraint so history survives user deletes / system actions allowed). `*_at` is always `TIMESTAMPTZ`.
  `created_at` is deliberately nullable (bulk imports may not know it); app
  inserts still default it to `now()` via JPA auditing + `@PrePersist`.
- Audit columns (`createdAt/createdBy/updatedAt/updatedBy/deletedAt/deletedBy`)
  are internal secrets — NEVER expose them in DTOs/JSON. Response DTOs carry
  only business fields + `uuid`.
- Soft delete via `deleted_at`/`deleted_by`. The filter is automatic through
  `@SQLRestriction("deleted_at IS NULL")` on `BaseEntity` (+ re-declared per
  entity) — do NOT add manual `AndDeletedAtIsNull` query suffixes.
  NEVER call `repository.delete*`; service layer soft-deletes via
  `entity.softDelete(actorId)` + save. Admin/restore flows needing deleted rows
  must use an explicit native-query escape hatch (e.g.
  `findIncludingDeletedByUuid`), never widen the default queries.
- JPA auditing is enabled (`config.JpaAuditingConfig` + `AuditorAware<Long>`);
  `createdAt/createdBy/updatedAt/updatedBy` are filled automatically.
  `deletedAt/deletedBy` are managed manually by the service. Until auth exists,
  `AuditorAware` returns empty (all `*_by` stay `NULL` for system actions).
- Any logical UNIQUE becomes a **partial unique index**, never a table-level `UNIQUE` constraint, e.g.:
  `CREATE UNIQUE INDEX IF NOT EXISTS <table>_<col>_uniq ON <table> (<col>) WHERE deleted_at IS NULL;`
  Same for composite uniques. Plain (non-unique) lookups on soft-deletable columns should also carry `WHERE deleted_at IS NULL` when the index supports it.

## DTO / API conventions

- All DTOs are Java `record`s.
- JSON is `snake_case` globally (`spring.jackson.property-naming-strategy:
  SNAKE_CASE`), e.g. `birth_date`, `next_cursor`, `has_next`. Java stays
  `camelCase`; never add per-field `@JsonProperty` for casing.
- Entity ↔ DTO mapping via MapStruct `@Mapper` interfaces only (no manual mapping); `componentModel = "spring"`, ignore `id` / `<ref>_id`, map FK as e.g. `@Mapping(target = "authorUuid", source = "author.uuid")`.
- Request/response DTOs carry `uuid` / `<ref>_uuid` only. `id` / `<ref>_id` fields are forbidden in DTOs and JSON (MapStruct mappers must ignore them; use a lookup for FK uuids).
- List endpoints use cursor pagination for infinite scroll: query params
  `cursor` (uuid of last row, omit for first page), `size` (1–100, default 20),
  `sort` (`field,dir`, default `id,desc`; allowed fields per endpoint).
  Response is `common.CursorPageResponse` (`content`, `nextCursor`, `hasNext`,
  `size`). Keyset logic lives in a `*CursorRepository` (Criteria API, `id DESC`
  tiebreaker); never use offset `Pageable` for FE lists.

## Gotchas — read before coding

- `src/main/resources/db/migration/` does **not** exist yet, but `application.yaml` sets `flyway.enabled: true` + `jpa.hibernate.ddl-auto: validate`. App/test context **fails to start** until you add a Flyway migration for every entity. Always create `V<n>__*.sql` alongside new entities.
- `@SpringBootTest contextLoads` requires live Postgres (`127.0.0.1:5432`, db `crud`, user/pass `admin`/`admin`) AND Valkey/Redis (`127.0.0.1:6379`, password `admin`). Start compose before `./gradlew test`.
- Config is env-overridable: `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `SPRING_DATA_REDIS_HOST/PORT/PASSWORD`. Defaults already match `docker-compose.yml`.
- MinIO image is `pgsty/silo:latest` (S3-compatible), not official MinIO; `mcli` CLI is available inside the container (used by `initMinio`).
- MapStruct `1.6.3` + Lombok are annotation-processor wired — keep `@Mapper` interfaces + `annotationProcessor` deps intact; plain `javac` without Gradle will miss generated impls.
- Virtual threads enabled (`spring.threads.virtual.enabled=true`); `open-in-view: false` — don't rely on lazy loading in controllers.
- CI (`.github/workflows/build.yml`) builds the jar then runs `docker build` with `context: .`, but there is **no Dockerfile** in repo yet — don't assume container build works.
