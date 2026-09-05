package com.damalert.ddas.monitoring.api;

import java.util.List;

public record PublicDamMapResponse(
	PublicDamResponse dam,
	List<PublicSensorResponse> sensors,
	List<PublicGateResponse> gates
) { }
