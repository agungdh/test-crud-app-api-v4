# Runtime saja: jar di-build oleh GH Action (./gradlew bootJar),
# image ini tinggal COPY fat jar ke distroless Debian 13 + Java 25.
# Cara pakai di CI: ./gradlew bootJar && docker build -t testcrud:ci .
FROM gcr.io/distroless/java25-debian13:nonroot
WORKDIR /app
COPY build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
