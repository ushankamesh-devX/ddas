package com.damalert.ddas.monitoring.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "iot_device_credential")
public class IotDeviceCredential {

	@Id
	private UUID id;

	@Column(name = "device_id", nullable = false)
	private UUID deviceId;

	@Column(name = "credential_type", nullable = false, length = 24)
	private String credentialType;

	@Column(name = "key_prefix", length = 32)
	private String keyPrefix;

	@Column(name = "credential_fingerprint", length = 128)
	private String credentialFingerprint;

	@Column(name = "secret_verifier", length = 500)
	private String secretVerifier;

	@Column(name = "broker_credential_ref", length = 255)
	private String brokerCredentialRef;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private CredentialStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "last_used_at")
	private Instant lastUsedAt;

	@Column(name = "rotated_at")
	private Instant rotatedAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	protected IotDeviceCredential() {
	}

	public IotDeviceCredential(UUID deviceId, String keyPrefix, String credentialFingerprint,
		String secretVerifier, String brokerCredentialRef) {
		this.id = UUID.randomUUID();
		this.deviceId = deviceId;
		this.credentialType = "DEVICE_KEY";
		this.keyPrefix = keyPrefix;
		this.credentialFingerprint = credentialFingerprint;
		this.secretVerifier = secretVerifier;
		this.brokerCredentialRef = brokerCredentialRef;
		this.status = CredentialStatus.ACTIVE;
		this.createdAt = Instant.now();
	}

	public void rotate() {
		this.status = CredentialStatus.ROTATED;
		this.rotatedAt = Instant.now();
	}

	public void revoke() {
		this.status = CredentialStatus.REVOKED;
		this.revokedAt = Instant.now();
	}

	public UUID getId() { return id; }
	public UUID getDeviceId() { return deviceId; }
	public String getKeyPrefix() { return keyPrefix; }
	public String getBrokerCredentialRef() { return brokerCredentialRef; }
	public CredentialStatus getStatus() { return status; }
}
