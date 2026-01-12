package com.example.ewallet.repository;

import com.example.ewallet.entity.InvestmentHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface InvestmentHistoryRepository extends MongoRepository<InvestmentHistory, String> {
    List<InvestmentHistory> findByUserId(String userId);    
    List<InvestmentHistory> findByFundId(String fundId);
}