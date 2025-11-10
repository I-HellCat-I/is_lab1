# --- ЭТАП 1: СБОРЩИК (Gradle-комбайн) ---
FROM gradle:8.5.0-jdk17-alpine AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN gradle build --no-daemon

# --- ЭТАП 2: ФИНАЛЬНЫЙ ОБРАЗ (Крейсер Payara) ---
FROM payara/server-full:6.2023.12-jdk17

ARG DB_HOST=db
ARG DB_PORT=5432
ARG DB_USER=comrade
ARG DB_PASSWORD=secret_password
ARG DB_NAME=kino_db

COPY --from=builder /app/build/libs/*.war /tmp/kino-app.war

# ИСПРАВЛЕНИЕ: МЫ БУДЕМ ВЫПОЛНЯТЬ КОМАНДЫ НА ЕЩЕ НЕ ЗАПУЩЕННОМ ДОМЕНЕ,
# ИЛИ ЗАПУСКАТЬ ЕГО В СПЕЦИАЛЬНОМ РЕЖИМЕ.
# Самый надежный способ - использовать post-boot команды.

# 1. Создаем файл с паролем для админа (даже если не будем его использовать, это хорошая практика)
RUN echo "AS_ADMIN_PASSWORD=" > /tmp/password.txt

# 2. Создаем файл с нашими командами, которые должны выполниться ПОСЛЕ старта сервера
RUN echo "create-jdbc-connection-pool --datasourceclassname org.postgresql.ds.PGSimpleDataSource --restype javax.sql.DataSource --property user=${DB_USER}:password=${DB_PASSWORD}:databaseName=${DB_NAME}:serverName=${DB_HOST}:portNumber=${DB_PORT} KinoConnectionPool" > /opt/payara/post-boot-commands.asadmin && \
    echo "create-jdbc-resource --connectionpoolid KinoConnectionPool jdbc/KinoDS" >> /opt/payara/post-boot-commands.asadmin && \
    echo "deploy --name kino-app /tmp/kino-app.war" >> /opt/payara/post-boot-commands.asadmin

# 3. Устанавливаем права на выполнение для скрипта
# RUN chmod +x /opt/payara/post-boot-commands.asadmin # Не обязательно, asadmin его читает

EXPOSE 8080 4848

# КОМАНДА ЗАПУСКА: Payara при старте автоматически подхватит и выполнит
# файл /opt/payara/post-boot-commands.asadmin.
# Ему не нужна аутентификация для этого на первом запуске.
CMD ["/opt/payara/appserver/bin/asadmin", "start-domain", "--verbose", "domain1"]