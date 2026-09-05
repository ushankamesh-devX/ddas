package com.damalert.ddas.evacuation.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.damalert.ddas.evacuation.domain.EvacuationRoute;
import com.damalert.ddas.evacuation.domain.RiskZone;
import com.damalert.ddas.evacuation.domain.SafeLocation;

public record PublicEvacuationSnapshot(
	UUID damId,
	Instant generatedAt,
	String version,
	boolean emergencyActive,
	List<RiskZone> riskZones,
	List<SafeLocation> safeLocations,
	List<EvacuationRoute> evacuationRoutes
) {
}
