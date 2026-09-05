package com.damalert.ddas.evacuation.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.evacuation.domain.DamEmergencyState;

public interface DamEmergencyStateRepository extends JpaRepository<DamEmergencyState, UUID> {
}
