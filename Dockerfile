# Многоступенчатая сборка для оптимизации размера образа

# Шаг 1: Сборка приложения
FROM gradle:8.5-jdk17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем только файлы сборки для кэширования зависимостей
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Копируем исходный код
COPY src ./src

# Собираем приложение, пропуская тесты (для ускорения)
RUN gradle build -x test --no-daemon

# Шаг 2: Финальный образ
FROM openjdk:17-jdk-slim

# Устанавливаем необходимые пакеты
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Создаем пользователя для запуска приложения (безопасность)
RUN groupadd -r app && \
    useradd -r -g app -m -s /bin/bash app

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR из предыдущего этапа
COPY --from=build /app/build/libs/*.jar app.jar

# Даем права на выполнение
RUN chown -R app:app /app

# Переключаемся на непривилегированного пользователя
USER app

# Настройки JVM для оптимальной работы
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:+UseContainerSupport"

# Открываем порт (обычно 8080 для Spring Boot)
EXPOSE 8080

# Точка входа
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]