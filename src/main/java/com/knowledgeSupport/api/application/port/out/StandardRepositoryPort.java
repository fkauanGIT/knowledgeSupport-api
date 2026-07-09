package com.knowledgeSupport.api.application.port.out;

import com.knowledgeSupport.api.domain.model.Standard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StandardRepositoryPort {
    Standard save(Standard standard);

    Optional<Standard> findById(UUID id);

    List<Standard> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
