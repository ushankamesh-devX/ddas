package com.damalert.alert.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AlertZoneId implements Serializable {
	@Column(name = "alert_id", nullable = false)
	private UUID alertId;

	@Column(name = "risk_zone_id", nullable = false)
	private UUID riskZoneId;
}