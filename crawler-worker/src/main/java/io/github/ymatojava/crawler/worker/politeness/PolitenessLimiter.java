package io.github.ymatojava.crawler.worker.politeness;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

/**
 * Компонент, реализующий политику вежливости (Politeness Policy).
 * 
 * Предотвращает DDoS-атаку на целевые сайты. Краулер не должен обращаться
 * к одному и тому же домену чаще, чем разрешено (например, 1 запрос в 2 секунды).
 * Использует Redis для координации задержек между распределенными воркерами.
 */
@Component
public class PolitenessLimiter {

    // Дефолтная задержка между запросами к одному домену (2 секунды)
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(2);
    
    private final StringRedisTemplate redisTemplate;

    public PolitenessLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Блокирует выполнение потока (если нужно), чтобы выдержать паузу
     * перед следующим запросом к домену.
     *
     * @param url URL, к которому планируется запрос
     */
    public void applyDelay(String url) {
        String domain = extractDomain(url);
        if (domain == null) return;

        String redisKey = "politeness:domain:" + domain;
        
        // Пытаемся установить ключ. Если ключ установлен (вернул true) — значит
        // никто не обращался к домену последние 2 секунды.
        // Если ключ уже есть (вернул false) — значит нужно подождать.
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(redisKey, "locked", DEFAULT_DELAY);
        
        if (Boolean.FALSE.equals(acquired)) {
            try {
                // В простейшей реализации просто спим.
                // В более продвинутой — возвращаем сообщение обратно в конец очереди RabbitMQ,
                // чтобы не блокировать worker-поток (Requeue).
                Thread.sleep(DEFAULT_DELAY.toMillis());
                
                // После сна снова ставим блокировку (здесь возможна гонка состояний в распределенной
                // среде, для production-ready используют Lua-скрипты или Redisson).
                redisTemplate.opsForValue().set(redisKey, "locked", DEFAULT_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return host != null ? host.toLowerCase() : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
