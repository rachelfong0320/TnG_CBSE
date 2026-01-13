package com.example.ewallet.repository;

import com.example.ewallet.entity.Fund;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface FundRepository extends MongoRepository<Fund, String> {
    List<Fund> findByRiskCategory(String riskCategory);
    
    boolean existsById(String id);

    boolean existsByNameIgnoreCase(String name);
    
    Optional<Fund> findByName(String name);
}