package com.example.ewallet.insurance.repository;

import com.example.ewallet.insurance.entity.BasePolicy;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PolicyRepository extends MongoRepository<BasePolicy, String> {
    // Check all of someone's insurance policies (whether they are motor, travel, or other types of insurance).
    List<BasePolicy> findByUserId(String userId);
}