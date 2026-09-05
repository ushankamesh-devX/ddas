package com.damalert.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.notification.entity.NotificationOutbox;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {
}