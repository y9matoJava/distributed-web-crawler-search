package io.github.ymatojava.crawler.worker.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * Компонент (Producer) для отправки сообщений в RabbitMQ.
 *
 * Изолирует логику работы с RabbitTemplate от остального приложения.
 */
@Service
public class UrlProducer {

    private static final Logger log = Logger.getLogger(UrlProducer.class.getName());

    private final RabbitTemplate rabbitTemplate;

    public UrlProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Отправляет задание на скачивание URL в основную очередь.
     *
     * @param message Объект задания
     */
    public void sendToReadyQueue(UrlMessage message) {
        log.info("Опубликовано сообщение в очередь: " + message.url() + " (глубина: " + message.depth() + ")");
        
        // Отправляем в конкретный Exchange с конкретным Routing Key
        rabbitTemplate.convertAndSend(
                RabbitConfig.CRAWLER_EXCHANGE,
                RabbitConfig.URL_READY_ROUTING_KEY,
                message
        );
    }
}
