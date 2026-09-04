package com.damalert.ddas.auth.application;

public record IssuedTokens(
	String accessToken,
	String refreshToken,
	long expiresIn
) {
}
