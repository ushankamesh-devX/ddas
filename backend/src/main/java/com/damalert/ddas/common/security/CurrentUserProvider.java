package com.damalert.ddas.common.security;

import java.util.Set;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.damalert.ddas.common.error.AuthenticationException;

@Component
@Profile("!standalone")
public class CurrentUserProvider {
	private final ActiveAccountChecker activeAccountChecker;

	public CurrentUserProvider(ActiveAccountChecker activeAccountChecker) {
		this.activeAccountChecker = activeAccountChecker;
	}

	public CurrentUser requireCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			throw new AuthenticationException("UNAUTHENTICATED", "Authentication is required.");
		}

		UUID userId;
		Set<String> roles;
		try {
			roles = Set.copyOf(jwt.getClaimAsStringList("roles"));
			userId = UUID.fromString(jwt.getSubject());
		}
		catch (RuntimeException exception) {
			throw new AuthenticationException("INVALID_TOKEN", "The access token is invalid.");
		}
		activeAccountChecker.requireActive(userId);
		return new CurrentUser(userId, jwt.getClaimAsString("email"), roles);
	}
}
