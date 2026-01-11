package com.example.ewallet.repository;

import com.example.ewallet.entity.ClaimRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ClaimRepository extends MongoRepository<ClaimRecord, String> {
    List<ClaimRecord> findByPolicyId(String policyId);
}
