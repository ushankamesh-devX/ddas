package com.damalert.ddas.monitoring.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.monitoring.domain.CredentialStatus;
import com.damalert.ddas.monitoring.domain.IotDeviceCredential;

public interface IotDeviceCredentialRepository extends JpaRepository<IotDeviceCredential, UUID> {
	Optional<IotDeviceCredential> findByDeviceIdAndStatus(UUID deviceId, CredentialStatus status);
}
