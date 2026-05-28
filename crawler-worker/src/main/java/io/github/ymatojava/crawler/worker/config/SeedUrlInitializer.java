package io.github.ymatojava.crawler.worker.config;

import io.github.ymatojava.crawler.common.model.UrlStatus;
import io.github.ymatojava.crawler.common.url.UrlHash;
import io.github.ymatojava.crawler.worker.entity.UrlEntity;
import io.github.ymatojava.crawler.worker.mq.UrlMessage;
import io.github.ymatojava.crawler.worker.mq.UrlProducer;
import io.github.ymatojava.crawler.worker.repository.UrlRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Инициализатор стартовой ссылки.
 * Выполняется автоматически при запуске Worker-а.
 * Если база пуста, он добавляет стартовую ссылку на Википедию,
 * чтобы краулер начал работу без ручного вмешательства.
 */
@Component
public class SeedUrlInitializer implements ApplicationRunner {

    private static final Logger log = Logger.getLogger(SeedUrlInitializer.class.getName());
    
    private final UrlRepository urlRepository;
    private final UrlProducer urlProducer;

    public SeedUrlInitializer(UrlRepository urlRepository, UrlProducer urlProducer) {
        this.urlRepository = urlRepository;
        this.urlProducer = urlProducer;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (urlRepository.count() == 0) {
            log.info("База данных URL пуста. Добавляем Seed URL для старта краулера...");
            
            String seedUrl = "https://ru.wikipedia.org/wiki/Java";
            String urlHash = UrlHash.sha256(seedUrl);
            
            UrlEntity entity = UrlEntity.discovered(seedUrl, urlHash);
            entity = urlRepository.save(entity);
            
            UrlMessage message = new UrlMessage(entity.getId(), seedUrl, 0);
            urlProducer.sendToReadyQueue(message);
            
            log.info("Seed URL успешно добавлен в базу и отправлен в очередь!");
        } else {
            log.info("База данных уже содержит URL. Краулер продолжит работу с текущего состояния.");
        }
    }
}
