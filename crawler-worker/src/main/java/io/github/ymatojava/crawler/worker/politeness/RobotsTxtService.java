package io.github.ymatojava.crawler.worker.politeness;

import io.github.ymatojava.crawler.core.download.DownloadResult;
import io.github.ymatojava.crawler.core.download.PageDownloader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Сервис для загрузки, парсинга и кэширования robots.txt.
 * Краулер ДОЛЖЕН уважать правила, описанные владельцами сайтов.
 */
@Service
public class RobotsTxtService {

    private static final Logger log = Logger.getLogger(RobotsTxtService.class.getName());
    
    // Кэшируем правила robots.txt в Redis на 24 часа, чтобы не скачивать их при каждом запросе
    private static final Duration CACHE_DURATION = Duration.ofHours(24);
    
    private final PageDownloader downloader;
    private final StringRedisTemplate redisTemplate;

    public RobotsTxtService(PageDownloader downloader, StringRedisTemplate redisTemplate) {
        this.downloader = downloader;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Проверяет, разрешен ли URL для обхода, согласно robots.txt сайта.
     *
     * @param url URL для проверки
     * @return true если обход разрешен, false если запрещен
     */
    public boolean isAllowed(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return false;
            
            String scheme = uri.getScheme();
            String path = uri.getPath() == null || uri.getPath().isEmpty() ? "/" : uri.getPath();
            
            // 1. Проверяем кэш в Redis
            String redisKey = "robots:" + host;
            String cachedRules = redisTemplate.opsForValue().get(redisKey);
            
            if (cachedRules == null) {
                // 2. Если в кэше нет — скачиваем robots.txt
                String robotsUrl = scheme + "://" + host + "/robots.txt";
                DownloadResult result = downloader.download(robotsUrl);
                
                if (result instanceof DownloadResult.Success s) {
                    cachedRules = s.body();
                } else {
                    // Если нет файла robots.txt (404) или ошибка скачивания, по умолчанию разрешаем
                    cachedRules = "ALLOW_ALL";
                }
                
                // 3. Сохраняем в кэш
                redisTemplate.opsForValue().set(redisKey, cachedRules, CACHE_DURATION);
            }
            
            if ("ALLOW_ALL".equals(cachedRules)) {
                return true;
            }
            
            // 4. Парсинг и проверка правил (упрощенная версия)
            // В реальном краулере нужен полноценный парсер (например, crawler-commons)
            return parseAndCheck(cachedRules, path);
            
        } catch (URISyntaxException e) {
            log.warning("Некорректный URL при проверке robots.txt: " + url);
            return false;
        }
    }

    /**
     * Простейший парсер robots.txt (только для правила User-agent: *).
     */
    private boolean parseAndCheck(String robotsContent, String path) {
        boolean isGlobalAgent = false;
        
        String[] lines = robotsContent.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            String lowerLine = line.toLowerCase();
            if (lowerLine.startsWith("user-agent:")) {
                String agent = line.substring(11).trim();
                isGlobalAgent = agent.equals("*");
            } else if (isGlobalAgent && lowerLine.startsWith("disallow:")) {
                String disallowPath = line.substring(9).trim();
                if (disallowPath.isEmpty()) continue; // Пустой Disallow означает Allow All
                
                // Преобразуем robots.txt path в regex (упрощенно)
                String regex = disallowPath.replace("*", ".*").replace("?", "\\?");
                if (Pattern.compile("^" + regex).matcher(path).find()) {
                    return false; // Нашли запрет
                }
            }
        }
        
        return true; // Разрешено по умолчанию
    }
}
