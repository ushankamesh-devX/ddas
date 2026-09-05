package com.damalert.ddas.monitoring.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.monitoring.domain.IotDevice;

public interface IotDeviceRepository extends JpaRepository<IotDevice, UUID> {
	List<IotDevice> findAllByDamIdOrderByNameAsc(UUID damId);
	Optional<IotDevice> findByIdAndDamId(UUID id, UUID damId);
	Optional<IotDevice> findByDeviceUid(String deviceUid);
}
