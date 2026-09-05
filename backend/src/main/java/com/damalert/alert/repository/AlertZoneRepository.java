package com.damalert.alert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.alert.entity.AlertZone;
import com.damalert.alert.entity.AlertZoneId;

public interface AlertZoneRepository extends JpaRepository<AlertZone, AlertZoneId> {
}