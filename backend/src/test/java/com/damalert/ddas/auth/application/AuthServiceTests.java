package com.damalert.ddas.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.damalert.ddas.auth.domain.AppUser;
import com.damalert.ddas.auth.persistence.AppUserRepository;
import com.damalert.ddas.auth.persistence.RefreshTokenRepository;
import com.damalert.ddas.common.error.AuthenticationException;

class AuthServiceTests {

	private AppUserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private TokenService tokenService;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		userRepository = mock(AppUserRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		tokenService = mock(TokenService.class);
		authService = new AuthService(
			userRepository,
			mock(RefreshTokenRepository.class),
			passwordEncoder,
			tokenService
		);
	}

	@Test
	void activeUserCanLogin() {
		AppUser user = new AppUser(UUID.randomUUID(), "admin@example.test", "hash", "Admin");
		IssuedTokens expected = new IssuedTokens("access", "refresh", 900);
		when(userRepository.findByEmailIgnoreCase("admin@example.test")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password", "hash")).thenReturn(true);
		when(tokenService.issue(user)).thenReturn(expected);

		IssuedTokens result = authService.login("admin@example.test", "password");

		assertThat(result).isSameAs(expected);
		verify(tokenService).issue(user);
	}

	@Test
	void badPasswordDoesNotIssueTokens() {
		AppUser user = new AppUser(UUID.randomUUID(), "admin@example.test", "hash", "Admin");
		when(userRepository.findByEmailIgnoreCase("admin@example.test")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

		assertThatThrownBy(() -> authService.login("admin@example.test", "wrong"))
			.isInstanceOf(AuthenticationException.class)
			.hasMessage("Email or password is incorrect.");
	}
}
