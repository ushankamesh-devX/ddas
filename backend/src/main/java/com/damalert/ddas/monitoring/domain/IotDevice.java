package com.damalert.ddas.monitoring.domain;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "iot_device")
public class IotDevice {

	@Id
	private UUID id;

	@Column(name = "dam_id", nullable = false)
	private UUID damId;

	@Column(name = "device_uid", nullable = false, unique = true, length = 160)
	private String deviceUid;

	@Column(nullable = false, length = 180)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "device_type", nullable = false, length = 40)
	private DeviceType deviceType;

	@Enumerated(EnumType.STRING)
	@Column(name = "auth_method", nullable = false, length = 24)
	private DeviceAuthMethod authMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "device_status", nullable = false, length = 24)
	private DeviceStatus status;

	@Column(name = "firmware_version", length = 100)
	private String firmwareVersion;

	@Column(name = "last_connected_at")
	private Instant lastConnectedAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@Column(name = "revoked_by")
	private UUID revokedBy;

	@Column(nullable = false, columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private String metadata;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected IotDevice() {
	}

	public IotDevice(UUID id, UUID damId, String name, DeviceType deviceType, DeviceAuthMethod authMethod) {
		Instant now = Instant.now();
		this.id = id;
		this.damId = damId;
		this.deviceUid = id.toString();
		this.name = name;
		this.deviceType = deviceType;
		this.authMethod = authMethod;
		this.status = DeviceStatus.ACTIVE;
		this.metadata = "{}";
		this.createdAt = now;
		this.updatedAt = now;
	}

	public void update(String name, DeviceType deviceType, String firmwareVersion) {
		if (status == DeviceStatus.REVOKED) {
			throw new IllegalStateException("A revoked device cannot be updated.");
		}
		this.name = name;
		this.deviceType = deviceType;
		this.firmwareVersion = firmwareVersion;
		this.updatedAt = Instant.now();
	}

	public void setStatus(DeviceStatus status, UUID actorUserId) {
		if (this.status == DeviceStatus.REVOKED && status != DeviceStatus.REVOKED) {
			throw new IllegalStateException("A revoked device cannot be re-enabled.");
		}
		this.status = status;
		this.updatedAt = Instant.now();
		if (status == DeviceStatus.REVOKED) {
			this.revokedAt = Instant.now();
			this.revokedBy = actorUserId;
		}
	}

	public void connected() {
		this.lastConnectedAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	public UUID getId() { return id; }
	public UUID getDamId() { return damId; }
	public String getDeviceUid() { return deviceUid; }
	public String getName() { return name; }
	public DeviceType getDeviceType() { return deviceType; }
	public DeviceAuthMethod getAuthMethod() { return authMethod; }
	public DeviceStatus getStatus() { return status; }
	public String getFirmwareVersion() { return firmwareVersion; }
	public Instant getLastConnectedAt() { return lastConnectedAt; }
	public Instant getRevokedAt() { return revokedAt; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
