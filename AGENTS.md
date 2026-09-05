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

## Gotchas — read before coding

- `src/main/resources/db/migration/` does **not** exist yet, but `application.yaml` sets `flyway.enabled: true` + `jpa.hibernate.ddl-auto: validate`. App/test context **fails to start** until you add a Flyway migration for every entity. Always create `V<n>__*.sql` alongside new entities.
- `@SpringBootTest contextLoads` requires live Postgres (`127.0.0.1:5432`, db `crud`, user/pass `admin`/`admin`) AND Valkey/Redis (`127.0.0.1:6379`, password `admin`). Start compose before `./gradlew test`.
- Config is env-overridable: `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `SPRING_DATA_REDIS_HOST/PORT/PASSWORD`. Defaults already match `docker-compose.yml`.
- MinIO image is `pgsty/silo:latest` (S3-compatible), not official MinIO; `mcli` CLI is available inside the container (used by `initMinio`).
- MapStruct `1.6.3` + Lombok are annotation-processor wired — keep `@Mapper` interfaces + `annotationProcessor` deps intact; plain `javac` without Gradle will miss generated impls.
- Virtual threads enabled (`spring.threads.virtual.enabled=true`); `open-in-view: false` — don't rely on lazy loading in controllers.
- CI (`.github/workflows/build.yml`) builds the jar then runs `docker build` with `context: .`, but there is **no Dockerfile** in repo yet — don't assume container build works.
