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
# Проверяем, как установлен docker compose (v2) или docker-compose (v1)
if command -v docker-compose &> /dev/null; then
    DOCKER_CMD="docker-compose"
elif docker --help | grep -q "compose"; then
    DOCKER_CMD="docker compose"
else
    echo "❌ Ошибка: Docker Compose не установлен на сервере!"
    echo "Пожалуйста, установите Docker и Docker Compose перед запуском:"
    echo "sudo apt update && sudo apt install docker.io docker-compose-v2 -y"
    exit 1
fi

$DOCKER_CMD -f docker-compose.prod.yml build
$DOCKER_CMD -f docker-compose.prod.yml up -d

echo "✅ Развертывание успешно завершено!"
echo "🔍 Проверьте логи: $DOCKER_CMD -f docker-compose.prod.yml logs -f"
