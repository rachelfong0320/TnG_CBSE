package com.example.ewallet.repository;

import com.example.ewallet.entity.AutoPayData;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AutoPayDataRepository extends MongoRepository<AutoPayData, String> {
    List<AutoPayData> findByUserId(String userId);
}
