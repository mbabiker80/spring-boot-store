package com.codewithmosh.store.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddItemToCartRequest {

	@NotNull(message = "Product ID is required")
	private long productId;
}
