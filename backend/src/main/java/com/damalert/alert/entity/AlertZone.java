package com.damalert.alert.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alert_zone")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertZone {

	@EmbeddedId
	private AlertZoneId id;
}