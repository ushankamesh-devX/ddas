package com.damalert.ddas.foundation;

import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.auth.domain.AppUser;
import com.damalert.ddas.auth.domain.Role;
import com.damalert.ddas.auth.persistence.AppUserRepository;
import com.damalert.ddas.auth.persistence.RoleRepository;
import com.damalert.ddas.dam.domain.Dam;
import com.damalert.ddas.dam.domain.DamStaff;
import com.damalert.ddas.dam.domain.DamStaffId;
import com.damalert.ddas.dam.domain.DamStaffRole;
import com.damalert.ddas.dam.persistence.DamRepository;
import com.damalert.ddas.dam.persistence.DamStaffRepository;

@Component
@ConditionalOnProperty(name = "app.fixtures.enabled", havingValue = "true")
public class LocalFixtures implements ApplicationRunner {

	public static final UUID DAM_A_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	public static final UUID DAM_B_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

	private final AppUserRepository userRepository;
	private final RoleRepository roleRepository;
	private final DamRepository damRepository;
	private final DamStaffRepository staffRepository;
	private final PasswordEncoder passwordEncoder;
	private final Environment environment;

	public LocalFixtures(
		AppUserRepository userRepository,
		RoleRepository roleRepository,
		DamRepository damRepository,
		DamStaffRepository staffRepository,
		PasswordEncoder passwordEncoder,
		Environment environment
	) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.damRepository = damRepository;
		this.staffRepository = staffRepository;
		this.passwordEncoder = passwordEncoder;
		this.environment = environment;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		String password = environment.getRequiredProperty("app.fixtures.password");
		Role superAdminRole = requireRole("SUPER_ADMIN");
		Role civilianRole = requireRole("CIVILIAN");

		ensureUser(
			"00000000-0000-0000-0000-000000000011",
			"admin@example.test",
			"Local Super Administrator",
			password,
			superAdminRole
		);
		ensureUser(
			"00000000-0000-0000-0000-000000000012",
			"civilian@example.test",
			"Local Civilian",
			password,
			civilianRole
		);
		AppUser damBAdmin = ensureUser(
			"00000000-0000-0000-0000-000000000013",
			"admin-b@example.test",
			"Dam B Administrator",
			password
		);
		AppUser operator = ensureUser(
			"00000000-0000-0000-0000-000000000014",
			"operator@example.test",
			"Dam A Operator",
			password
		);
		AppUser engineer = ensureUser(
			"00000000-0000-0000-0000-000000000015",
			"engineer@example.test",
			"Dam A Engineer",
			password
		);

		ensureDam(DAM_A_ID, "DAM-A", "Development Dam A");
		ensureDam(DAM_B_ID, "DAM-B", "Development Dam B");
		ensureStaff(DAM_A_ID, operator.getId(), DamStaffRole.DAM_OPERATOR, true);
		ensureStaff(DAM_A_ID, engineer.getId(), DamStaffRole.DAM_ENGINEER, false);
		ensureStaff(DAM_B_ID, damBAdmin.getId(), DamStaffRole.DAM_ADMIN, true);

		// The super administrator intentionally has platform-wide access without a dam_staff row.
		// The civilian intentionally has no dam staff assignment.
	}

	private AppUser ensureUser(String id, String email, String name, String password, Role... roles) {
		return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
			AppUser user = new AppUser(UUID.fromString(id), email, passwordEncoder.encode(password), name);
			for (Role role : roles) {
				user.addRole(role);
			}
			return userRepository.save(user);
		});
	}

	private void ensureDam(UUID id, String code, String name) {
		if (!damRepository.existsById(id)) {
			damRepository.save(new Dam(id, code, name, "Deterministic local/CI fixture", true));
		}
	}

	private void ensureStaff(UUID damId, UUID userId, DamStaffRole role, boolean canTriggerEmergency) {
		DamStaffId id = new DamStaffId(damId, userId);
		if (!staffRepository.existsById(id)) {
			staffRepository.save(new DamStaff(id, role, canTriggerEmergency));
		}
	}

	private Role requireRole(String code) {
		return roleRepository.findByCode(code)
			.orElseThrow(() -> new IllegalStateException("Required seeded role is missing: " + code));
	}
}
