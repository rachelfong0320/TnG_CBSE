package com.example.ewallet.insurance.repository;

import com.example.ewallet.insurance.entity.ClaimRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ClaimRepository extends MongoRepository<ClaimRecord, String> {
    List<ClaimRecord> findByPolicyId(String policyId);
}
