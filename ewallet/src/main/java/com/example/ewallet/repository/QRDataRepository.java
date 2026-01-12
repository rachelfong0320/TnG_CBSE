package com.example.ewallet.repository;

import com.example.ewallet.entity.QRData;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface QRDataRepository extends MongoRepository<QRData, String> {
    List<QRData> findByUserId(String userId);
}
