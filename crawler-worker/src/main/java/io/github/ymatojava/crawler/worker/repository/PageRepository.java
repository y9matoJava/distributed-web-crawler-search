package io.github.ymatojava.crawler.worker.repository;

import io.github.ymatojava.crawler.worker.entity.PageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к таблице crawl_pages.
 */
@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {
}
