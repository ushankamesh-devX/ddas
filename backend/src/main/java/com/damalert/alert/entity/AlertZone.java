package com.damalert.alert.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "alert_zone")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AlertZone { @EmbeddedId private AlertZoneId id; }