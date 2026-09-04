package com.damalert.ddas.auth.api;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.ddas.auth.application.AuthService;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.common.security.CurrentUserProvider;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final CurrentUserProvider currentUserProvider;

	public AuthController(AuthService authService, CurrentUserProvider currentUserProvider) {
		this.authService = authService;
		this.currentUserProvider = currentUserProvider;
	}

	@PostMapping("/login")
	TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return TokenResponse.from(authService.login(request.email(), request.password()));
	}

	@PostMapping("/refresh")
	TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return TokenResponse.from(authService.refresh(request.refreshToken()));
	}

	@PostMapping("/logout")
	ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
		authService.logout(request.refreshToken());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	CurrentUserResponse me() {
		CurrentUser currentUser = currentUserProvider.requireCurrentUser();
		return CurrentUserResponse.from(authService.requireUser(currentUser.userId()));
	}
}
