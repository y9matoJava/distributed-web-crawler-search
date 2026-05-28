package io.github.ymatojava.crawler.worker.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация RabbitMQ.
 * 
 * Настраивает обменники (Exchanges), очереди (Queues) и связи (Bindings)
 * для маршрутизации URL-адресов. Также включает поддержку Dead Letter Queue (DLQ)
 * для обработки сообщений, которые не удалось обработать после нескольких попыток.
 */
@Configuration
public class RabbitConfig {

    public static final String CRAWLER_EXCHANGE = "crawler.exchange";
    
    // Основная очередь для ссылок, ожидающих скачивания
    public static final String URL_READY_QUEUE = "crawler.url.ready";
    public static final String URL_READY_ROUTING_KEY = "url.ready";
    
    // Очередь для "мертвых" сообщений (содержащих ошибки)
    public static final String DLQ_QUEUE = "crawler.url.dlq";
    public static final String DLQ_ROUTING_KEY = "url.dlq";

    /**
     * Конвертер сообщений в JSON формат (по умолчанию используется бинарная сериализация Java).
     * Это позволяет легко читать сообщения в RabbitMQ Management UI.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Основной обменник типа Direct.
     * Отправляет сообщения строго в ту очередь, routingKey которой точно совпадает
     * с routingKey сообщения.
     */
    @Bean
    public DirectExchange crawlerExchange() {
        return new DirectExchange(CRAWLER_EXCHANGE);
    }

    /**
     * Основная очередь ссылок на скачивание.
     * Настроена с указанием Dead Letter Exchange — если обработка сообщения завершится ошибкой
     * (исключение в consumer или reject без requeue), оно будет отправлено в DLQ.
     */
    @Bean
    public Queue urlReadyQueue() {
        return QueueBuilder.durable(URL_READY_QUEUE)
                .withArgument("x-dead-letter-exchange", CRAWLER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    /**
     * Dead Letter Queue (DLQ) — очередь недоставленных/ошибочных сообщений.
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    /**
     * Связываем основную очередь с обменником.
     */
    @Bean
    public Binding urlReadyBinding() {
        return BindingBuilder.bind(urlReadyQueue()).to(crawlerExchange()).with(URL_READY_ROUTING_KEY);
    }

    /**
     * Связываем DLQ с обменником.
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(crawlerExchange()).with(DLQ_ROUTING_KEY);
    }
}
