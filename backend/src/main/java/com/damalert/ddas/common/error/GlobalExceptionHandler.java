package com.damalert.ddas.common.error;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ApiError> handleApiException(ApiException exception, HttpServletRequest request) {
		return ResponseEntity.status(exception.status())
			.body(error(exception.code(), exception.getMessage(), exception.details(), request));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		Map<String, Object> fields = new LinkedHashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			fields.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return ResponseEntity.badRequest()
			.body(error("VALIDATION_FAILED", "Request validation failed.", Map.of("fields", fields), request));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
		return ResponseEntity.badRequest()
			.body(error("INVALID_REQUEST_BODY", "The request body is malformed.", Map.of(), request));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
		logger.error("Unhandled request failure", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(error("INTERNAL_ERROR", "An unexpected error occurred.", Map.of(), request));
	}

	private ApiError error(
		String code,
		String message,
		Map<String, Object> details,
		HttpServletRequest request
	) {
		Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
		return new ApiError(code, message, details, String.valueOf(requestId), Instant.now());
	}
}
