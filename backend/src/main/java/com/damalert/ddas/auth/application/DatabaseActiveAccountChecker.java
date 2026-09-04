package com.damalert.ddas.auth.application;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.auth.domain.AccountStatus;
import com.damalert.ddas.auth.domain.AppUser;
import com.damalert.ddas.auth.persistence.AppUserRepository;
import com.damalert.ddas.common.error.AuthenticationException;
import com.damalert.ddas.common.security.ActiveAccountChecker;

@Component
@Profile("!standalone")
@Transactional(readOnly = true)
public class DatabaseActiveAccountChecker implements ActiveAccountChecker {

	private final AppUserRepository userRepository;

	public DatabaseActiveAccountChecker(AppUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void requireActive(UUID userId) {
		AppUser user = userRepository.findById(userId)
			.orElseThrow(() -> new AuthenticationException("INVALID_TOKEN", "The access token user no longer exists."));
		if (user.getAccountStatus() != AccountStatus.ACTIVE) {
			throw new AuthenticationException("ACCOUNT_INACTIVE", "The account is not active.");
		}
	}
}
