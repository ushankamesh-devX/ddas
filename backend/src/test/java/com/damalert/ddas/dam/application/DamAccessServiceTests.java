package com.damalert.ddas.dam.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.damalert.ddas.common.error.ForbiddenException;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.domain.DamStaff;
import com.damalert.ddas.dam.domain.DamStaffId;
import com.damalert.ddas.dam.domain.DamStaffRole;
import com.damalert.ddas.dam.persistence.DamStaffRepository;

class DamAccessServiceTests {

	private final DamStaffRepository repository = mock(DamStaffRepository.class);
	private final DamAccessService service = new DamAccessService(repository);

	@Test
	void superAdminHasPlatformWideAccess() {
		CurrentUser superAdmin = new CurrentUser(UUID.randomUUID(), "admin@example.test", Set.of("SUPER_ADMIN"));

		assertThatCode(() -> service.requireStaffAccess(superAdmin, UUID.randomUUID())).doesNotThrowAnyException();
	}

	@Test
	void assignedOperatorHasAccessToOwnDam() {
		UUID userId = UUID.randomUUID();
		UUID damId = UUID.randomUUID();
		CurrentUser operator = new CurrentUser(userId, "operator@example.test", Set.of());
		when(repository.findByDamIdAndUserId(damId, userId)).thenReturn(Optional.of(
			new DamStaff(new DamStaffId(damId, userId), DamStaffRole.DAM_OPERATOR, true)
		));

		assertThatCode(() -> service.requireStaffAccess(operator, damId)).doesNotThrowAnyException();
	}

	@Test
	void staffFromAnotherDamIsRejected() {
		UUID userId = UUID.randomUUID();
		UUID damB = UUID.randomUUID();
		CurrentUser operator = new CurrentUser(userId, "operator@example.test", Set.of());
		when(repository.findByDamIdAndUserId(damB, userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.requireStaffAccess(operator, damB))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("You do not have access to this dam.");
	}
}
