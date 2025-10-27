# --- ЭТАП 1: СБОРЩИК (Комсомольская стройка) ---
# Используем Node.js для сборки нашего фронтенда
FROM node:22.12-alpine AS builder

WORKDIR /app

# Кэшируем зависимости
COPY package.json package-lock.json ./
RUN npm install

# Копируем исходники и собираем статику
COPY . .
RUN npm run build

# --- ЭТАП 2: ФИНАЛЬНЫЙ ОБРАЗ (Агит-поезд) ---
# Используем легкий, как перышко, образ Nginx
FROM nginx:1.25-alpine

# Копируем наш кастомный конфиг для Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Копируем ТОЛЬКО собранную статику из этапа сборщика
# Папка `dist` - это результат `npm run build`
COPY --from=builder /app/dist /usr/share/nginx/html

# Открываем порт 80 для HTTP-трафика
EXPOSE 80

# Команда по умолчанию для запуска Nginx
CMD ["nginx", "-g", "daemon off;"]