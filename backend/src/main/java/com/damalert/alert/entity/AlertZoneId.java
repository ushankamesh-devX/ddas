package com.damalert.alert.entity;

import java.io.Serializable;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Embeddable @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class AlertZoneId implements Serializable {
	@Column(name = "alert_id", nullable = false) private UUID alertId;
	@Column(name = "risk_zone_id", nullable = false) private UUID riskZoneId;
}