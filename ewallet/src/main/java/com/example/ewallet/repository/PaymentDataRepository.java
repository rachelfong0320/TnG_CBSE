package com.example.ewallet.repository;

import com.example.ewallet.entity.PaymentData;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PaymentDataRepository extends MongoRepository<PaymentData, String> {
    List<PaymentData> findByUserId(String userId);
}