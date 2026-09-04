package com.damalert.ddas.auth.api;

import java.util.Set;
import java.util.UUID;

import com.damalert.ddas.auth.domain.AppUser;

public record CurrentUserResponse(
	UUID id,
	String email,
	String displayName,
	Set<String> roles
) {
	static CurrentUserResponse from(AppUser user) {
		return new CurrentUserResponse(
			user.getId(),
			user.getEmail(),
			user.getDisplayName(),
			user.getRoles().stream().map(role -> role.getCode()).collect(java.util.stream.Collectors.toUnmodifiableSet())
		);
	}
}
