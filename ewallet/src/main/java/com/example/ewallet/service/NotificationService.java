package com.example.ewallet.service;

import com.example.ewallet.entity.*;
import com.example.ewallet.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    private String getUserId(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        return user != null ? user.getId() : null;
    }

    public void generateNotification(String phoneNumber, String type, String message) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user != null) {
            Notification notification = new Notification(user.getId(), user.getUsername(), phoneNumber, type, message);
            notificationRepository.save(notification);
        }
    }

    public List<Notification> getAllNotifications(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user == null)
            return List.of();
        return notificationRepository.findByUserIdOrderByTimestampAsc(user.getId());
    }

    public List<Notification> getUnreadNotifications(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user == null)
            return List.of();
        return notificationRepository.findByUserIdAndReadOrderByTimestampAsc(user.getId(), false);
    }

    public long getUnreadCount(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user == null)
            return 0;
        return notificationRepository.countByUserIdAndRead(user.getId(), false);
    }

    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void markAllAsRead(String phoneNumber) {
        getUnreadNotifications(phoneNumber).forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
        public void displayNotifications(String phoneNumber) {
        List<Notification> notifications = getAllNotifications(phoneNumber);
        long unreadCount = getUnreadCount(phoneNumber);

        System.out.println("\n=== NOTIFICATIONS ===");
        System.out.printf("Total: %d | Unread: %d%n", notifications.size(), unreadCount);
        System.out.println("--------------------");

        if (notifications.isEmpty()) {
            System.out.println("No notifications yet.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        for (int i = 0; i < notifications.size(); i++) {
            Notification notif = notifications.get(i);
            String readStatus = notif.isRead() ? "[ ]" : "[*]";
            System.out.printf("%d. %s [%s] %s%n", i + 1, readStatus, notif.getType(),
                    notif.getTimestamp().format(formatter));
            System.out.printf("   %s%n", notif.getMessage());
        }
    }
}
