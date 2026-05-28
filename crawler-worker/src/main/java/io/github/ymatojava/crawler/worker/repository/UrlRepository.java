package io.github.ymatojava.crawler.worker.repository;

import io.github.ymatojava.crawler.worker.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для доступа к таблице crawl_urls.
 * 
 * Spring Data JPA автоматически генерирует реализацию этого интерфейса.
 */
@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    /**
     * Поиск URL по его SHA-256 хешу.
     * Используется для проверки, существует ли уже такой URL в базе.
     *
     * @param urlHash SHA-256 хеш
     * @return Optional с найденной сущностью
     */
    Optional<UrlEntity> findByUrlHash(String urlHash);
    
    /**
     * Проверяет существование записи по хешу.
     * Работает быстрее, чем findByUrlHash, так как делает запрос вида SELECT EXISTS.
     *
     * @param urlHash SHA-256 хеш
     * @return true, если URL уже есть в базе
     */
    boolean existsByUrlHash(String urlHash);
}
