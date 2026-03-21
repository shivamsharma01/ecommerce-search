package com.mcart.search.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps validation and infrastructure errors to HTTP responses with stable JSON bodies.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
						(a, b) -> a,
						LinkedHashMap::new));
		return ResponseEntity.badRequest().body(Map.of(
				"error", "validation_failed",
				"fields", fields));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest().body(Map.of(
				"error", "invalid_request_body",
				"message", "Request body must be valid JSON"));
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<Map<String, String>> handleDataAccess(DataAccessException ex) {
		log.warn("Elasticsearch data access failure: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
				"error", "search_unavailable",
				"message", "Search is temporarily unavailable"));
	}
}
