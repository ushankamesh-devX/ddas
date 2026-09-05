package com.damalert.ddas.monitoring.application;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.common.error.NotFoundException;
import com.damalert.ddas.dam.domain.Dam;
import com.damalert.ddas.dam.persistence.DamRepository;
import com.damalert.ddas.monitoring.api.PublicDamMapResponse;
import com.damalert.ddas.monitoring.api.PublicDamResponse;

@Service
@Profile("!standalone")
@Transactional(readOnly = true)
public class PublicDamService {
	private final DamRepository dams;
	private final SensorService sensors;
	private final GateService gates;

	public PublicDamService(DamRepository dams, SensorService sensors, GateService gates) {
		this.dams = dams;
		this.sensors = sensors;
		this.gates = gates;
	}

	public List<PublicDamResponse> list() {
		return dams.findAllByPublicVisibleTrueAndActiveTrueOrderByNameAsc().stream()
			.map(PublicDamResponse::from).toList();
	}

	public PublicDamResponse get(UUID damId) {
		return PublicDamResponse.from(requirePublic(damId));
	}

	public PublicDamMapResponse map(UUID damId) {
		PublicDamResponse dam = PublicDamResponse.from(requirePublic(damId));
		return new PublicDamMapResponse(dam, sensors.listPublic(damId), gates.listPublic(damId));
	}

	private Dam requirePublic(UUID damId) {
		Dam dam = dams.findById(damId)
			.orElseThrow(() -> new NotFoundException("DAM_NOT_FOUND", "Dam does not exist."));
		if (!dam.isPublicVisible() || !dam.isActive()) {
			throw new NotFoundException("DAM_NOT_FOUND", "Dam does not exist.");
		}
		return dam;
	}
}
