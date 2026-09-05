plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "id.my.agungdh"
version = "0.0.1-SNAPSHOT"
description = "test-crud-app-api-v4"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    runtimeOnly("org.postgresql:postgresql")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Docker helper tasks (pengganti target Makefile init-minio / recreate-postgres).
// Nilai bisa di-override via environment, mis. MINIO_USER=foo ./gradlew initMinio
val minioUser: String = providers.environmentVariable("MINIO_USER").getOrElse("admin")
val minioPassword: String = providers.environmentVariable("MINIO_PASSWORD").getOrElse("admin123")
val minioEndpoint: String = providers.environmentVariable("MINIO_ENDPOINT").getOrElse("http://127.0.0.1:9000")
val minioAlias: String = providers.environmentVariable("MINIO_ALIAS").getOrElse("local")
val appBucket: String = providers.environmentVariable("BUCKET").getOrElse("testcrud")

tasks.register<Exec>("initMinio") {
    group = "docker"
    description = "Init MinIO bucket (idempotent, default: testcrud)"
    commandLine(
        "sh", "-c",
        "docker compose exec -T minio mcli alias set $minioAlias $minioEndpoint $minioUser $minioPassword" +
                " && docker compose exec -T minio mcli mb --ignore-existing $minioAlias/$appBucket" +
                " && docker compose exec -T minio mcli ls $minioAlias/"
    )
}

tasks.register<Exec>("recreatePostgres") {
    group = "docker"
    description = "Stop postgres, hapus volume, start lagi (DATA HILANG)"
    commandLine(
        "sh", "-c",
        "docker compose stop postgres && docker compose rm -f -v postgres && docker compose up -d postgres"
    )
}
