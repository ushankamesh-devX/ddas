package com.damalert.alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.alert.entity.AlertZone;
import com.damalert.alert.entity.AlertZoneId;

public interface AlertZoneRepository extends JpaRepository<AlertZone, AlertZoneId> {

	List<AlertZone> findByIdAlertId(UUID alertId);

	List<AlertZone> findByIdRiskZoneId(UUID riskZoneId);
}