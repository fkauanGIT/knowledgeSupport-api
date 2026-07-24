package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Standard;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StandardPersistenceAdapter implements StandardRepositoryPort {

    private final StandardJpaRepository standardJpaRepository;

    public StandardPersistenceAdapter(StandardJpaRepository standardJpaRepository) {
        this.standardJpaRepository = standardJpaRepository;
    }

    @Override
    @CacheEvict(value = "standards", allEntries = true)
    public Standard save(Standard standard) {
        StandardJpaEntity saved = standardJpaRepository.save(StandardMapper.toEntity(standard));
        return StandardMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Standard> findById(UUID id) {
        return standardJpaRepository.findById(id).map(StandardMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("standards")
    public List<Standard> findAll() {
        return standardJpaRepository.findAll().stream().map(StandardMapper::toDomain).toList();
    }

    @Override
    @CacheEvict(value = "standards", allEntries = true)
    public void deleteById(UUID id) {
        standardJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return standardJpaRepository.existsById(id);
    }
}
