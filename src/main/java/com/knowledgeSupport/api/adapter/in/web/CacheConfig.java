package com.knowledgeSupport.api.adapter.in.web;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Cache Caffeine com TTLs por cache (o {@code spec} do yaml é global demais):
 * <ul>
 *   <li><b>openCalleds</b>: chamados vindos do Jira ao vivo — staleness curto aceitável (Jira
 *       segue sendo a fonte da verdade), TTL 45s corta a maioria das idas ao Jira.</li>
 *   <li><b>standards</b>: base de conhecimento lida a cada análise; invalidada por escrita
 *       (create/update/delete). TTL longo é só rede de segurança.</li>
 *   <li><b>documentCorpus</b>: corpus tokenizado da busca TF-IDF — caro de recomputar;
 *       invalidado no upload/delete de documento. TTL longo é só rede de segurança.</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        // default para caches não registrados explicitamente (ex.: openCalleds): 45s
        manager.setCaffeine(Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofSeconds(45)));
        manager.registerCustomCache("standards",
                Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofMinutes(10)).build());
        manager.registerCustomCache("documentCorpus",
                Caffeine.newBuilder().maximumSize(16).expireAfterWrite(Duration.ofMinutes(30)).build());
        return manager;
    }
}
