package com.example.repository;

import com.example.entity.EmailEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailEventRepository extends JpaRepository<EmailEvent, Long> {
    long countByEmailAndEventType(String email, String eventType);
    long countByEmailAndEventTypeAndUrl(String email, String eventType, String url);
}
