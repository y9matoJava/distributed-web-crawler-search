package io.github.ymatojava.crawler.worker.mq;

/**
 * Неизменяемый объект (DTO) для передачи по сети через RabbitMQ.
 * 
 * В отличие от Entity (которая привязана к JPA) или внутренних DTO,
 * этот класс сериализуется в JSON и отправляется в брокер сообщений.
 * Содержит минимально необходимый набор данных для запуска обработки URL.
 *
 * @param urlId Уникальный ID записи в базе данных (crawl_urls)
 * @param url   Сам URL-адрес
 * @param depth Текущая глубина обхода (используется для ограничения рекурсии)
 */
public record UrlMessage(
        Long urlId,
        String url,
        int depth
) {
    public UrlMessage {
        if (urlId == null) {
            throw new IllegalArgumentException("urlId не может быть null");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url не может быть пустым");
        }
        if (depth < 0) {
            throw new IllegalArgumentException("Глубина обхода не может быть отрицательной");
        }
    }
}
