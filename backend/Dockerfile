FROM gradle:8.14-jdk21 AS build
WORKDIR /home/gradle/project

# 1. Копируем ТОЛЬКО файлы, описывающие зависимости
COPY build.gradle settings.gradle ./

# 2. Скачиваем зависимости. Этот слой будет кэшироваться, пока build.gradle не изменится.
# Команда 'dependencies' просто резолвит и скачивает все зависимости.
RUN gradle dependencies --no-daemon

# 3. Теперь, когда зависимости в кэше, копируем исходный код
COPY src ./src

# 4. Запускаем полную сборку. Gradle использует уже скачанные зависимости.
# Эта команда будет быстрой, т.к. не нужно ничего качать.
RUN gradle build --no-daemon -x test

# Этап 2: Создание образa
FROM eclipse-temurin:21-jre
WORKDIR /app
# Копируем собранный JAR-файл из этапа сборки
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
# Запускаем приложение через java -jar
ENTRYPOINT ["java","-jar","app.jar"]