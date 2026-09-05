package com.damalert.ddas.evacuation.application;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.common.audit.AuditEvent;
import com.damalert.ddas.common.audit.AuditService;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.evacuation.domain.DamEmergencyState;
import com.damalert.ddas.evacuation.persistence.DamEmergencyStateRepository;

/**
 * Emergency operational state per dam.
 *
 * <p>This is deliberately independent of {@code dam.operational_state}, which Dev 1 owns.
 * Alert severity and emergency operational state are related but not identical concepts.
 *
 * <p>Activate and clear are idempotent: requesting a transition that has already happened
 * returns the current state unchanged and records no second audit event. This is deliberate
 * for a life-safety action. Refusing a duplicate activation with a conflict would mean an
 * operator sees an error during an active emergency, and the correct response to "the
 * emergency is already active" is to carry on, not to fail the request. The stored
 * {@code idempotency_key} records which request produced the current state.
 */
@Service
@Profile("!standalone")
@Transactional
public class EmergencyService {

	private final DamEmergencyStateRepository stateRepository;
	private final DamAccessChecker accessChecker;
	private final DamReader damReader;
	private final AuditService auditService;

	public EmergencyService(
		DamEmergencyStateRepository stateRepository,
		DamAccessChecker accessChecker,
		DamReader damReader,
		AuditService auditService
	) {
		this.stateRepository = stateRepository;
		this.accessChecker = accessChecker;
		this.damReader = damReader;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public DamEmergencyState get(CurrentUser user, UUID damId) {
		accessChecker.requireStaffAccess(user, damId);
		damReader.requireDam(damId);
		return stateRepository.findById(damId).orElseGet(() -> new DamEmergencyState(damId));
	}

	@Transactional(readOnly = true)
	public DamEmergencyState getPublicState(UUID damId) {
		return stateRepository.findById(damId).orElseGet(() -> new DamEmergencyState(damId));
	}

	public DamEmergencyState activate(CurrentUser user, UUID damId, String reason, String idempotencyKey) {
		accessChecker.requireEmergencyPermission(user, damId);
		damReader.requireDam(damId);

		DamEmergencyState state = stateRepository.findById(damId)
			.orElseGet(() -> new DamEmergencyState(damId));

		if (state.isActive()) {
			return state;
		}

		state.activate(user.userId(), reason, idempotencyKey);
		DamEmergencyState saved = stateRepository.saveAndFlush(state);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"EMERGENCY_ACTIVATED",
			"dam_emergency_state",
			damId,
			Map.of("state", "INACTIVE"),
			details(saved, reason)
		));
		return saved;
	}

	public DamEmergencyState clear(CurrentUser user, UUID damId, String reason, String idempotencyKey) {
		accessChecker.requireEmergencyPermission(user, damId);
		damReader.requireDam(damId);

		DamEmergencyState state = stateRepository.findById(damId)
			.orElseGet(() -> new DamEmergencyState(damId));

		if (!state.isActive()) {
			return state;
		}

		state.clear(user.userId(), reason, idempotencyKey);
		DamEmergencyState saved = stateRepository.saveAndFlush(state);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"EMERGENCY_CLEARED",
			"dam_emergency_state",
			damId,
			Map.of("state", "ACTIVE"),
			details(saved, reason)
		));
		return saved;
	}

	private Map<String, Object> details(DamEmergencyState state, String reason) {
		Map<String, Object> details = new HashMap<>();
		details.put("state", state.getState().name());
		details.put("reason", reason);
		return details;
	}
}
