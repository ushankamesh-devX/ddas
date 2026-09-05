package com.damalert.ddas.evacuation.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.common.security.CurrentUserProvider;
import com.damalert.ddas.evacuation.application.SafeLocationService;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/dams/{damId}/safe-locations")
public class SafeLocationController {

	private final SafeLocationService safeLocationService;
	private final CurrentUserProvider currentUserProvider;
	private final GeometryMapper geometryMapper;

	public SafeLocationController(
		SafeLocationService safeLocationService,
		CurrentUserProvider currentUserProvider,
		GeometryMapper geometryMapper
	) {
		this.safeLocationService = safeLocationService;
		this.currentUserProvider = currentUserProvider;
		this.geometryMapper = geometryMapper;
	}

	@GetMapping
	List<SafeLocationResponse> list(@PathVariable UUID damId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return safeLocationService.list(user, damId).stream()
			.map(safeLocation -> SafeLocationResponse.from(safeLocation, geometryMapper))
			.toList();
	}

	@GetMapping("/{safeLocationId}")
	SafeLocationResponse get(@PathVariable UUID damId, @PathVariable UUID safeLocationId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return SafeLocationResponse.from(safeLocationService.get(user, damId, safeLocationId), geometryMapper);
	}

	@PostMapping
	ResponseEntity<SafeLocationResponse> create(
		@PathVariable UUID damId,
		@Valid @RequestBody CreateSafeLocationRequest request
	) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		SafeLocationResponse created = SafeLocationResponse.from(
			safeLocationService.create(
				user,
				damId,
				request.code(),
				request.name(),
				request.location(),
				request.capacity(),
				request.currentOccupancy(),
				request.status(),
				request.contactNumber(),
				request.facilities(),
				request.publicVisibleOrDefault(),
				request.instructions()
			),
			geometryMapper
		);
		return ResponseEntity
			.created(URI.create("/api/v1/dams/" + damId + "/safe-locations/" + created.id()))
			.body(created);
	}

	@PatchMapping("/{safeLocationId}")
	SafeLocationResponse update(
		@PathVariable UUID damId,
		@PathVariable UUID safeLocationId,
		@Valid @RequestBody UpdateSafeLocationRequest request
	) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return SafeLocationResponse.from(
			safeLocationService.update(
				user,
				damId,
				safeLocationId,
				request.name(),
				request.location(),
				request.capacity(),
				request.currentOccupancy(),
				request.status(),
				request.contactNumber(),
				request.facilities(),
				request.publicVisible(),
				request.instructions()
			),
			geometryMapper
		);
	}

	@DeleteMapping("/{safeLocationId}")
	ResponseEntity<Void> delete(@PathVariable UUID damId, @PathVariable UUID safeLocationId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		safeLocationService.delete(user, damId, safeLocationId);
		return ResponseEntity.noContent().build();
	}
}
