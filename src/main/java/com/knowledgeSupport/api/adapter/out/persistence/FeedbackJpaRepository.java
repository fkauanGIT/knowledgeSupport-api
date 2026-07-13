package com.knowledgeSupport.api.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedbackJpaRepository extends JpaRepository<FeedbackJpaEntity, UUID> {
    List<FeedbackJpaEntity> findByStandardId(UUID standardId);
}
