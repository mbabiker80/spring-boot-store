package com.codewithmosh.store.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {
	@NotNull
	private String name;
	private String email;
}
