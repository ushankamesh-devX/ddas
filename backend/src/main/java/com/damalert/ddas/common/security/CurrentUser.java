package com.damalert.ddas.common.security;

import java.util.Set;
import java.util.UUID;

public record CurrentUser(
	UUID userId,
	String email,
	Set<String> globalRoles
) {
	public boolean hasGlobalRole(String role) {
		return globalRoles.contains(role);
	}
}
