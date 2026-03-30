package com.openjar.notificationservice.repository;

import com.openjar.notificationservice.models.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationLog, Long> {}