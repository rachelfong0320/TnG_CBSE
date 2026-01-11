package com.example.ewallet.repository;

import com.example.ewallet.entity.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WalletRepository extends MongoRepository<Wallet, String> {
    Wallet findByUserId(String userId);
}
