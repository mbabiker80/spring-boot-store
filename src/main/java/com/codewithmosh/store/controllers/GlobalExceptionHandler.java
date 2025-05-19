package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.ErrorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice //advice for our controlle, anytime an exception is thrown in one of our controllers
// this class will catch that exception
public class GlobalExceptionHandler {

@ExceptionHandler(HttpMessageNotReadableException.class)
	//public ResponseEntity<Map<String,String>> handleUnreadableMessage(Exception exception){
	public ResponseEntity<ErrorDto> handleUnreadableMessage(Exception exception){

		return ResponseEntity.badRequest().body(
				new ErrorDto("Invalid request body.")
				//Map.of("error","Invalid request body.")
		);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	//"name": "Name is required"  //return Object Structure Map
	public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException exception){
		var errors = new HashMap<String, String>();
		exception.getBindingResult().getFieldErrors().forEach(error -> {
			errors.put(error.getField(), error.getDefaultMessage());
		});

		return ResponseEntity.badRequest().body(errors);
	}
}
