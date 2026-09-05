package com.damalert.ddas.evacuation.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Cacheable public evacuation snapshot.
 *
 * <p>{@code generatedAt} and {@code version} exist so the mobile client can cache this
 * payload offline and detect staleness without refetching the whole graph.
 */
public record PublicEvacuationSnapshotResponse(
	UUID damId,
	Instant generatedAt,
	String version,
	boolean emergencyActive,
	List<PublicRiskZoneResponse> riskZones,
	List<PublicSafeLocationResponse> safeLocations,
	List<PublicEvacuationRouteResponse> evacuationRoutes
) {
}
