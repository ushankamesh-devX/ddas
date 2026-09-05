package com.damalert.ddas.evacuation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.damalert.ddas.common.audit.AuditEvent;
import com.damalert.ddas.common.audit.AuditService;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.evacuation.domain.DamEmergencyState;
import com.damalert.ddas.evacuation.domain.EmergencyStatus;
import com.damalert.ddas.evacuation.persistence.DamEmergencyStateRepository;

class EmergencyServiceTests {

	private final DamEmergencyStateRepository repository = mock(DamEmergencyStateRepository.class);
	private final DamAccessChecker accessChecker = mock(DamAccessChecker.class);
	private final DamReader damReader = mock(DamReader.class);
	private final AuditService auditService = mock(AuditService.class);
	private final EmergencyService service =
		new EmergencyService(repository, accessChecker, damReader, auditService);

	private final UUID damId = UUID.randomUUID();
	private final CurrentUser operator = new CurrentUser(UUID.randomUUID(), "operator@example.test", Set.of());

	@Test
	void activationCreatesStateAndRecordsAudit() {
		when(repository.findById(damId)).thenReturn(Optional.empty());
		when(repository.saveAndFlush(any(DamEmergencyState.class))).thenAnswer(call -> call.getArgument(0));

		DamEmergencyState state = service.activate(operator, damId, "Spillway failure", "key-1");

		assertThat(state.getState()).isEqualTo(EmergencyStatus.ACTIVE);
		assertThat(state.getActivatedBy()).isEqualTo(operator.userId());
		assertThat(state.getActivatedAt()).isNotNull();
		verify(auditService).record(any(AuditEvent.class));
	}

	@Test
	void activationRequiresEmergencyPermissionNotJustStaffAccess() {
		when(repository.saveAndFlush(any(DamEmergencyState.class))).thenAnswer(call -> call.getArgument(0));

		service.activate(operator, damId, "reason", "key-1");

		verify(accessChecker).requireEmergencyPermission(operator, damId);
		verify(accessChecker, never()).requireStaffAccess(any(), any());
	}

	@Test
	void replayingTheSameIdempotencyKeyDoesNotActivateTwice() {
		DamEmergencyState active = new DamEmergencyState(damId);
		active.activate(operator.userId(), "Spillway failure", "key-1");
		when(repository.findById(damId)).thenReturn(Optional.of(active));

		DamEmergencyState replayed = service.activate(operator, damId, "Spillway failure", "key-1");

		assertThat(replayed.getState()).isEqualTo(EmergencyStatus.ACTIVE);
		verify(repository, never()).saveAndFlush(any());
		verify(auditService, never()).record(any());
	}

	@Test
	void activatingAnAlreadyActiveEmergencyIsANoOpRatherThanAnError() {
		DamEmergencyState active = new DamEmergencyState(damId);
		active.activate(operator.userId(), "Spillway failure", "key-1");
		when(repository.findById(damId)).thenReturn(Optional.of(active));

		// A second operator activating during a live emergency must not receive an error.
		DamEmergencyState result = service.activate(operator, damId, "Different incident", "key-2");

		assertThat(result.getState()).isEqualTo(EmergencyStatus.ACTIVE);
		assertThat(result.getReason()).isEqualTo("Spillway failure");
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void clearingAnActiveEmergencyIsAudited() {
		DamEmergencyState active = new DamEmergencyState(damId);
		active.activate(operator.userId(), "Spillway failure", "key-1");
		when(repository.findById(damId)).thenReturn(Optional.of(active));
		when(repository.saveAndFlush(any(DamEmergencyState.class))).thenAnswer(call -> call.getArgument(0));

		DamEmergencyState cleared = service.clear(operator, damId, "Water level normal", "key-clear");

		assertThat(cleared.getState()).isEqualTo(EmergencyStatus.INACTIVE);
		assertThat(cleared.getClearedBy()).isEqualTo(operator.userId());
		assertThat(cleared.getClearedAt()).isNotNull();
		verify(auditService).record(any(AuditEvent.class));
	}

	@Test
	void clearingWhenNoEmergencyIsActiveIsANoOp() {
		when(repository.findById(damId)).thenReturn(Optional.of(new DamEmergencyState(damId)));

		DamEmergencyState result = service.clear(operator, damId, "nothing to clear", "key-clear");

		assertThat(result.getState()).isEqualTo(EmergencyStatus.INACTIVE);
		verify(repository, never()).saveAndFlush(any());
		verify(auditService, never()).record(any());
	}
}
