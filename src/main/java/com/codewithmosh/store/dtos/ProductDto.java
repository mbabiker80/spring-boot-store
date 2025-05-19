package com.codewithmosh.store.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Data
public class ProductDto {
	@JsonProperty("product_id")
	private Long id;
	private String name;
	private String description;
	private String price;
	private byte categoryId;
}
