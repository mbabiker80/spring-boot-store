package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.AddItemToCartRequest;
import com.codewithmosh.store.dtos.CartDto;
import com.codewithmosh.store.dtos.CartItemDto;
import com.codewithmosh.store.dtos.UpdateCartItemRequest;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/carts")
@Tag(name = "Carts", description = "Operations for carts")
public class CartController {
//	private final CartRepository cartRepository;
//	private final CartMapper cartMapper;
//	private final ProductRepository productRepository;
	private final CartService cartService;

	@PostMapping()
	public ResponseEntity<CartDto> createCart(
			UriComponentsBuilder uriBuilder
	){
		//refactor
		var cartDto = cartService.createCart();

		var uri = uriBuilder.path("/carts/{id}").buildAndExpand(cartDto.getId()).toUri();

		//return new ResponseEntity<>(cartDto, HttpStatus.CREATED,);
		return ResponseEntity.created(uri).body(cartDto);
	}

	@Operation(summary = "Add item to cart")//Adds a product to the cart
	@PostMapping("/{cartId}/items")
	public ResponseEntity<CartItemDto> addToCart(
			@Parameter(description = "The cart id") //The ID of the cart.
			@PathVariable UUID cartId,
			@RequestBody AddItemToCartRequest request
	){

		var cartItemDto = cartService.addItemToCart(cartId, request.getProductId());

		return ResponseEntity.status(HttpStatus.CREATED).body(cartItemDto);
	}

	@GetMapping("/{cartId}")
	//public ResponseEntity<CartDto> getCart(@PathVariable UUID cartId){
	public CartDto getCart(@PathVariable UUID cartId){

		//var cartDto = cartService.getCart(cartId);

		//return ResponseEntity.ok(cartDto);
		return cartService.getCart(cartId);
	}

@PutMapping("/{cartId}/items/{productId}")
//public ResponseEntity<?> updateCartItem(
	public CartItemDto updateCartItem(
			@PathVariable("cartId") UUID cartId,
			@PathVariable("productId") Long productId,
			@Valid @RequestBody UpdateCartItemRequest request
		){

		//var cartItemDto = cartService.updateCartItem(cartId, productId, request.getQuantity());
		//return ResponseEntity.ok(cartItemDto);

		return cartService.updateCartItem(cartId, productId, request.getQuantity());



	}

	@DeleteMapping("/{cartId}/items/{productId}")
	public ResponseEntity<?> removeItem(
			@PathVariable UUID cartId,
			@PathVariable Long productId
	){
		cartService.removeItem(cartId, productId);

		return ResponseEntity.noContent().build();

	}

	@DeleteMapping("/{cartId}/items")
	public ResponseEntity<?> clearCart(@PathVariable UUID cartId){
		cartService.clearCart(cartId);

		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(value = {CartNotFoundException.class})
	public ResponseEntity<Map<String, String>> handleCartNotFoundException(){
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cart not found.")); //Cart was not found.

		//return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(value = {ProductNotFoundException.class})
	public ResponseEntity<Map<String, String>> handleProductNotFoundException(){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Product  not found in the cart.")); //was
	}

}
