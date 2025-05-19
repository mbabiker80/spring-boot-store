package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.ProductDto;
import com.codewithmosh.store.mappers.ProductMapper;
import com.codewithmosh.store.repositories.CategoryRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@AllArgsConstructor
public class ProductController {
	private final ProductRepository productRepository;
	private final ProductMapper productMapper;
	private final CategoryRepository categoryRepository;

	@GetMapping("/product/{id}")
	public ResponseEntity<ProductDto> getProductById(@PathVariable Long id){
		var product = productRepository.findById(id).orElse(null);
		if (product == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(productMapper.toDto(product));

		//return ResponseEntity.ok(new ProductDTO(product.getName(), product.getDescription(), product.getPrice().toString(), product.getCategory().getName()));

	}

	@PostMapping("/product")
	public ResponseEntity<ProductDto> createProduct(
			@RequestBody ProductDto productDto,
			UriComponentsBuilder uriBuilder){

		var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
		if (category == null) {
			return ResponseEntity.badRequest().build();
		}

		var product = productMapper.toEntity(productDto);
		product.setCategory(category); //category
		productRepository.save(product);
		productDto.setId(product.getId());

		//return ResponseEntity.ok(productDto);

		var uri = uriBuilder.path("/product/{id}").buildAndExpand(product.getId()).toUri();

		return ResponseEntity.created(uri).body(productDto);
	}

	@PutMapping("/product/{id}")
	public ResponseEntity<ProductDto> updateProduct(
			@PathVariable Long id,
			@RequestBody ProductDto productDto){
		var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
		if (category == null) {
			return ResponseEntity.badRequest().build();
		}

		var product = productRepository.findById(id).orElse(null);
		if (product == null) {
			return ResponseEntity.notFound().build();
		}
		productMapper.update(productDto, product);
		product.setCategory(category); //category
		productRepository.save(product);
		productDto.setId(product.getId()); //id for return

		return ResponseEntity.ok(productDto); //without mapping from product
	}

	@DeleteMapping("/product/{id}")
	public ResponseEntity<?> deleteProduct(@PathVariable Long id){
		var product = productRepository.findById(id).orElse(null);
		if (product == null) {
			return ResponseEntity.notFound().build();
		}
		productRepository.delete(product);
		return ResponseEntity.noContent().build();
	}

}
