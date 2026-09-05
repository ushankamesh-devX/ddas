package com.damalert.ddas.monitoring.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.common.audit.AuditEvent;
import com.damalert.ddas.common.audit.AuditService;
import com.damalert.ddas.common.error.ConflictException;
import com.damalert.ddas.common.error.NotFoundException;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.dam.domain.DamStaffRole;
import com.damalert.ddas.monitoring.api.GateRequest;
import com.damalert.ddas.monitoring.api.GateResponse;
import com.damalert.ddas.monitoring.api.PublicGateResponse;
import com.damalert.ddas.monitoring.domain.DamGate;
import com.damalert.ddas.monitoring.domain.SensorVisibility;
import com.damalert.ddas.monitoring.persistence.DamGateRepository;

@Service
@Profile("!standalone")
@Transactional
public class GateService {
	private final DamGateRepository gates;
	private final DamReader dams;
	private final DamAccessChecker access;
	private final GeometryMapper geometry;
	private final AuditService audit;

	public GateService(DamGateRepository gates, DamReader dams, DamAccessChecker access,
		GeometryMapper geometry, AuditService audit) {
		this.gates = gates;
		this.dams = dams;
		this.access = access;
		this.geometry = geometry;
		this.audit = audit;
	}

	@Transactional(readOnly = true)
	public List<GateResponse> list(CurrentUser user, UUID damId) {
		access.requireStaffAccess(user, damId);
		return gates.findAllByDamIdOrderByNameAsc(damId).stream().map(GateResponse::from).toList();
	}

	public GateResponse create(CurrentUser user, UUID damId, GateRequest request) {
		requireManage(user, damId);
		dams.requireDam(damId);
		String code = request.code().trim().toUpperCase();
		if (gates.existsByDamIdAndCode(damId, code)) {
			throw new ConflictException("GATE_CODE_EXISTS", "A gate with this code already exists for the dam.");
		}
		Point point = request.location() == null ? null : geometry.toPoint(request.location());
		DamGate gate = gates.saveAndFlush(new DamGate(UUID.randomUUID(), damId, code, request.name().trim(),
			point, request.status(), request.openingPercent(), request.visibility()));
		record(user, damId, "GATE_CREATED", gate.getId());
		return GateResponse.from(gate);
	}

	public GateResponse update(CurrentUser user, UUID damId, UUID gateId, GateRequest request) {
		requireManage(user, damId);
		DamGate gate = require(damId, gateId);
		if (!gate.getCode().equalsIgnoreCase(request.code().trim())) {
			throw new com.damalert.ddas.common.error.BadRequestException("GATE_CODE_IMMUTABLE", "Gate code cannot be changed.");
		}
		gate.update(request.name().trim(), request.location() == null ? null : geometry.toPoint(request.location()),
			request.status(), request.openingPercent(), request.visibility());
		record(user, damId, "GATE_UPDATED", gateId);
		return GateResponse.from(gate);
	}

	public void delete(CurrentUser user, UUID damId, UUID gateId) {
		requireManage(user, damId);
		gates.delete(require(damId, gateId));
		record(user, damId, "GATE_DELETED", gateId);
	}

	@Transactional(readOnly = true)
	public List<PublicGateResponse> listPublic(UUID damId) {
		return gates.findAllByDamIdAndVisibilityNotOrderByNameAsc(damId, SensorVisibility.PRIVATE).stream()
			.map(PublicGateResponse::from).toList();
	}

	private DamGate require(UUID damId, UUID gateId) {
		return gates.findByIdAndDamId(gateId, damId)
			.orElseThrow(() -> new NotFoundException("GATE_NOT_FOUND", "Gate does not exist."));
	}

	private void requireManage(CurrentUser user, UUID damId) {
		access.requireRole(user, damId, DamStaffRole.DAM_ADMIN, DamStaffRole.DAM_ENGINEER, DamStaffRole.DAM_OPERATOR);
	}

	private void record(CurrentUser user, UUID damId, String action, UUID id) {
		audit.record(new AuditEvent(damId, user.userId(), action, "dam_gate", id, null, Map.of()));
	}
}
