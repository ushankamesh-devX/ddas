package com.damalert.ddas.common.security;

import java.util.UUID;

public interface ActiveAccountChecker {

	void requireActive(UUID userId);
}
