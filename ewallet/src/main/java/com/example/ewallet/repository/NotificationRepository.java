package com.example.ewallet.repository;

import com.example.ewallet.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUsernameOrderByTimestampAsc(String username);
    List<Notification> findByUsernameOrderByTimestampDesc(String username);
    List<Notification> findByUsernameAndReadOrderByTimestampDesc(String username, boolean read);
    long countByUsernameAndRead(String username, boolean read);
}
