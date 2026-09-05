package com.damalert.ddas.monitoring.api;

import java.math.BigDecimal;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.monitoring.domain.SensorVisibility;
import com.damalert.ddas.monitoring.domain.ThresholdDirection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateSensorRequest {
	@NotBlank @Size(max = 100)
	private String code;
	@NotBlank @Size(max = 180)
	private String name;
	@NotBlank @Size(max = 48)
	private String sensorType;
	@NotBlank @Size(max = 32)
	private String unit;
	@Valid
	private GeoJsonPoint location;
	private SensorVisibility visibility = SensorVisibility.PRIVATE;
	private boolean exposeExactLocation;
	private BigDecimal warningThreshold;
	private BigDecimal criticalThreshold;
	private ThresholdDirection thresholdDirection = ThresholdDirection.HIGH;

	public String code() { return code; }
	public String name() { return name; }
	public String sensorType() { return sensorType; }
	public String unit() { return unit; }
	public GeoJsonPoint location() { return location; }
	public SensorVisibility visibility() { return visibility; }
	public boolean exposeExactLocation() { return exposeExactLocation; }
	public BigDecimal warningThreshold() { return warningThreshold; }
	public BigDecimal criticalThreshold() { return criticalThreshold; }
	public ThresholdDirection thresholdDirection() { return thresholdDirection; }

	public void setCode(String code) { this.code = code; }
	public void setName(String name) { this.name = name; }
	public void setSensorType(String sensorType) { this.sensorType = sensorType; }
	public void setUnit(String unit) { this.unit = unit; }
	public void setLocation(GeoJsonPoint location) { this.location = location; }
	public void setVisibility(SensorVisibility visibility) { this.visibility = visibility; }
	public void setExposeExactLocation(boolean exposeExactLocation) { this.exposeExactLocation = exposeExactLocation; }
	public void setWarningThreshold(BigDecimal warningThreshold) { this.warningThreshold = warningThreshold; }
	public void setCriticalThreshold(BigDecimal criticalThreshold) { this.criticalThreshold = criticalThreshold; }
	public void setThresholdDirection(ThresholdDirection thresholdDirection) { this.thresholdDirection = thresholdDirection; }
}
