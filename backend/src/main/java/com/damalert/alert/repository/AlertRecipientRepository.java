package com.damalert.alert.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.alert.entity.AlertRecipient;

public interface AlertRecipientRepository extends JpaRepository<AlertRecipient, UUID> {
}