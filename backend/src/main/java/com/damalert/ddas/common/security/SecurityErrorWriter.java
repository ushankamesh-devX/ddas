package com.damalert.ddas.common.security;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;

final class SecurityErrorWriter {

	private SecurityErrorWriter() {
	}

	static void write(HttpServletResponse response, int status, String code, String message) throws IOException {
		String requestId = response.getHeader("X-Request-Id");
		if (requestId == null) {
			requestId = UUID.randomUUID().toString();
			response.setHeader("X-Request-Id", requestId);
		}
		response.setStatus(status);
		response.setContentType("application/json");
		response.getWriter().printf(
			"{\"code\":\"%s\",\"message\":\"%s\",\"details\":{},\"requestId\":\"%s\",\"timestamp\":\"%s\"}",
			code,
			message,
			requestId,
			Instant.now()
		);
	}
}
