# === СТАДИЯ 1: СБОРКА ===
# Используем официальный образ Eclipse Temurin с JDK 17 для компиляции кода
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# Копируем файлы сборки Gradle (кэшируем зависимости, чтобы не качать их при каждом изменении кода)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Даем права на выполнение и скачиваем зависимости
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Копируем исходный код и собираем JAR
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# === СТАДИЯ 2: ЗАПУСК ===
# Используем минимальный образ JRE для финального контейнера (значительно меньше по размеру)
FROM eclipse-temurin:17-jre-jammy

# Создаем пользователя без прав root для безопасности
RUN groupadd -r appuser && useradd -r -g appuser -d /app appuser

WORKDIR /app

# Копируем собранный JAR из предыдущей стадии
COPY --from=builder /build/build/libs/*.jar app.jar

# Переключаемся на непривилегированного пользователя
USER appuser

# Настройки JVM, адаптированные под ограничения контейнера (важно для Render)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Healthcheck для платформ типа Render
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Точка входа
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]