#!/bin/bash

echo "🚀 Начинаем развертывание Distributed Web Crawler Search..."

# Проверяем наличие .env файла
if [ ! -f .env ]; then
    echo "⚠️ Ошибка: Файл .env не найден!"
    echo "Пожалуйста, скопируйте .env.example в .env и настройте пароли:"
    echo "cp .env.example .env"
    echo "nano .env"
    exit 1
fi

echo "📦 Сборка и запуск контейнеров..."
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d

echo "✅ Развертывание успешно завершено!"
echo "🔍 Проверьте логи: docker-compose -f docker-compose.prod.yml logs -f"
