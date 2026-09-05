package com.damalert.ddas.monitoring.persistence;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.monitoring.domain.IotDeviceSensor;
import com.damalert.ddas.monitoring.domain.IotDeviceSensorId;

public interface IotDeviceSensorRepository extends JpaRepository<IotDeviceSensor, IotDeviceSensorId> {
	boolean existsByIdDeviceIdAndIdSensorId(UUID deviceId, UUID sensorId);
	Optional<IotDeviceSensor> findByIdSensorId(UUID sensorId);
	List<IotDeviceSensor> findAllByIdDeviceId(UUID deviceId);
	void deleteByIdDeviceIdAndIdSensorId(UUID deviceId, UUID sensorId);
}
