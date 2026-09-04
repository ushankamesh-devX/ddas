package com.damalert.ddas.dam.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.common.security.CurrentUserProvider;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamService;
import com.damalert.ddas.dam.domain.DamStaff;
import com.damalert.ddas.dam.persistence.DamStaffRepository;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/dams")
public class DamController {

	private final DamService damService;
	private final DamAccessChecker accessChecker;
	private final DamStaffRepository staffRepository;
	private final CurrentUserProvider currentUserProvider;

	public DamController(
		DamService damService,
		DamAccessChecker accessChecker,
		DamStaffRepository staffRepository,
		CurrentUserProvider currentUserProvider
	) {
		this.damService = damService;
		this.accessChecker = accessChecker;
		this.staffRepository = staffRepository;
		this.currentUserProvider = currentUserProvider;
	}

	@GetMapping
	List<DamResponse> list() {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return damService.listAccessible(user).stream().map(DamResponse::from).toList();
	}

	@PostMapping
	ResponseEntity<DamResponse> create(@Valid @RequestBody CreateDamRequest request) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		DamResponse created = DamResponse.from(
			damService.create(user, request.code(), request.name(), request.description(), request.isPublic())
		);
		return ResponseEntity.created(URI.create("/api/v1/dams/" + created.id())).body(created);
	}

	@GetMapping("/{damId}")
	DamResponse get(@PathVariable UUID damId) {
		return DamResponse.from(damService.getAccessible(currentUserProvider.requireCurrentUser(), damId));
	}

	@GetMapping("/{damId}/staff")
	List<DamStaffResponse> staff(@PathVariable UUID damId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		accessChecker.requireStaffAccess(user, damId);
		List<DamStaff> staff = staffRepository.findAllByDamId(damId);
		return staff.stream().map(DamStaffResponse::from).toList();
	}
}
