package com.damalert.ddas.common.error;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {

	public AuthenticationException(String code, String message) {
		super(HttpStatus.UNAUTHORIZED, code, message);
	}
}
