package io.github.ymatojava.crawler.worker.mq;

import io.github.ymatojava.crawler.worker.pipeline.WorkerPipeline;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Consumer (Слушатель) RabbitMQ.
 * 
 * Постоянно слушает очередь URL_READY_QUEUE.
 * Когда появляется новое сообщение, Spring AMQP автоматически десериализует JSON
 * в объект UrlMessage и вызывает метод receive().
 */
@Component
public class RabbitUrlConsumer {

    private static final Logger log = Logger.getLogger(RabbitUrlConsumer.class.getName());

    private final WorkerPipeline workerPipeline;

    public RabbitUrlConsumer(WorkerPipeline workerPipeline) {
        this.workerPipeline = workerPipeline;
    }

    /**
     * Метод-обработчик сообщений из очереди.
     * 
     * Если метод завершается успешно (без исключений) — сообщение удаляется из очереди (ack).
     * Если выбрасывается исключение — сообщение возвращается в очередь (nack)
     * или отправляется в DLQ в зависимости от настроек RabbitMQ.
     *
     * @param message Десериализованное сообщение
     */
    @RabbitListener(queues = RabbitConfig.URL_READY_QUEUE)
    public void receive(UrlMessage message) {
        log.info("Получено сообщение из RabbitMQ: " + message);
        
        try {
            workerPipeline.process(message);
        } catch (Exception e) {
            log.severe("Ошибка при обработке сообщения " + message + ": " + e.getMessage());
            // Пробрасываем исключение дальше, чтобы Spring AMQP мог отработать логику DLQ
            throw e;
        }
    }
}
