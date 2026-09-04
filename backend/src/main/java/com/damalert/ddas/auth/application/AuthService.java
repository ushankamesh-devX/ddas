package com.damalert.ddas.auth.application;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.auth.domain.AccountStatus;
import com.damalert.ddas.auth.domain.AppUser;
import com.damalert.ddas.auth.domain.RefreshToken;
import com.damalert.ddas.auth.persistence.AppUserRepository;
import com.damalert.ddas.auth.persistence.RefreshTokenRepository;
import com.damalert.ddas.common.error.AuthenticationException;

@Service
@Profile("!standalone")
@Transactional
public class AuthService {

	private final AppUserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	public AuthService(
		AppUserRepository userRepository,
		RefreshTokenRepository refreshTokenRepository,
		PasswordEncoder passwordEncoder,
		TokenService tokenService
	) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
	}

	public IssuedTokens login(String email, String password) {
		AppUser user = userRepository.findByEmailIgnoreCase(email)
			.orElseThrow(this::invalidCredentials);
		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw invalidCredentials();
		}
		requireActive(user);
		return tokenService.issue(user);
	}

	public IssuedTokens refresh(String rawRefreshToken) {
		RefreshToken token = refreshTokenRepository.findByTokenHash(tokenService.hash(rawRefreshToken))
			.orElseThrow(() -> new AuthenticationException("INVALID_REFRESH_TOKEN", "The refresh token is invalid."));
		if (token.isRevoked() || !token.getExpiresAt().isAfter(Instant.now())) {
			throw new AuthenticationException("INVALID_REFRESH_TOKEN", "The refresh token is invalid or expired.");
		}
		AppUser user = userRepository.findById(token.getUserId())
			.orElseThrow(() -> new AuthenticationException("INVALID_REFRESH_TOKEN", "The refresh token is invalid."));
		requireActive(user);
		token.revoke();
		return tokenService.issue(user);
	}

	public void logout(String rawRefreshToken) {
		refreshTokenRepository.findByTokenHash(tokenService.hash(rawRefreshToken)).ifPresent(RefreshToken::revoke);
	}

	@Transactional(readOnly = true)
	public AppUser requireUser(java.util.UUID userId) {
		AppUser user = userRepository.findById(userId)
			.orElseThrow(() -> new AuthenticationException("INVALID_TOKEN", "The access token user no longer exists."));
		requireActive(user);
		return user;
	}

	private void requireActive(AppUser user) {
		if (user.getAccountStatus() != AccountStatus.ACTIVE) {
			throw new AuthenticationException("ACCOUNT_INACTIVE", "The account is not active.");
		}
	}

	private AuthenticationException invalidCredentials() {
		return new AuthenticationException("INVALID_CREDENTIALS", "Email or password is incorrect.");
	}
}
