package com.damalert.ddas.auth.api;

import com.damalert.ddas.auth.application.IssuedTokens;

public record TokenResponse(
	String accessToken,
	String refreshToken,
	String tokenType,
	long expiresIn
) {
	static TokenResponse from(IssuedTokens tokens) {
		return new TokenResponse(tokens.accessToken(), tokens.refreshToken(), "Bearer", tokens.expiresIn());
	}
}
