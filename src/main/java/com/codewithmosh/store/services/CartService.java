package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CartDto;
import com.codewithmosh.store.dtos.CartItemDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class CartService {

	private CartRepository cartRepository;
	private CartMapper cartMapper;
	private ProductRepository productRepository;

	public CartDto createCart(){
		var cart = new Cart();

		cartRepository.save(cart);

		return cartMapper.toDto(cart);
	}

	public CartItemDto addItemToCart(UUID cartId, Long productId){

		//var cart = cartRepository.findById(cartId).orElse(null);
		var cart = cartRepository.getCartWithItems(cartId).orElse(null);
		if (cart == null) {
			//return ResponseEntity.notFound().build();
			throw new CartNotFoundException();//"Cart was not found."
		}

		var product = productRepository.findById(productId).orElse(null);
		if (product == null) {
			throw new ProductNotFoundException(); //"Product was not found in the cart."
			//return ResponseEntity.badRequest().build(); //because the issue is with the data the client sent
		}

		//move the logic to the class that have the necessary data "cart"
		//anemic domain model only have data
		//rich domain model have data and behavior
		var cartItem = cart.addItem(product);

//		var cartItem = cart.getItem(product.getId());
////		var cartItem = cart.getItems().stream()
////				.filter(item -> item.getProduct().getId().equals(product.getId()))
////				.findFirst()
////				.orElse(null);
////				//.anyMatch(item -> item.getProduct().getId() == product.getId())
//
//		if (cartItem != null) {
//			cartItem.setQuantity(cartItem.getQuantity() + 1);
//		} else {
//			cartItem = new CartItem();
//			cartItem.setProduct(product);
//			cartItem.setQuantity(1);
//			cartItem.setCart(cart);
//			cart.getItems().add(cartItem);
//		}

		cartRepository.save(cart);

		return cartMapper.toDto(cartItem);
	}

	public CartDto getCart(UUID cartId){

		//var cart = cartRepository.findById(cartId).orElse(null);
		var cart = cartRepository.findById(cartId).orElse(null);
		if (cart == null) {
			throw new CartNotFoundException();
		}
		return cartMapper.toDto(cart);
	}

	public CartItemDto updateCartItem(UUID cartId, Long productId, Integer quantity){
//		var cart = cartRepository.findById(cartId).orElse(null);
//		if (cart == null) {
//			throw new CartNotFoundException();
//		}
//		var cartItem = cart.getItem(productId);
//		if (cartItem == null) {
//			throw new ProductNotFoundException();
//		}
//		cartItem.setQuantity(request.getQuantity());
//		cartRepository.save(cart);
//		return cartMapper.toDto(cartItem);

		var cart = cartRepository.getCartWithItems(cartId).orElse(null);
		if (cart == null) {
			throw new CartNotFoundException();
			//return ResponseEntity.notFound().build();
		}

		var cartItem = cart.getItem(productId);

//		var cartItem = cart.getItems().stream()
//				.filter(item -> item.getProduct().getId().equals(productId))
//				.findFirst()
//				.orElse(null);

		if (cartItem == null) {
//			//return ResponseEntity.notFound().build();
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//					Map.of("message", "Product was not found in the cart.")
//			);
			throw new ProductNotFoundException();
		}

		cartItem.setQuantity(quantity);
		cartRepository.save(cart);

		return cartMapper.toDto(cartItem);
	}

	public void removeItem(UUID cartId, Long productId){

		var cart = cartRepository.getCartWithItems(cartId).orElse(null);
		if (cart == null) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//					Map.of("error", "Cart was not found.")
//			);
			throw new CartNotFoundException();
		}

		cart.removeItem(productId);

//		var cartItem = cart.getItem(productId);
//		if (cartItem != null) {
//			cart.getItems().remove(cartItem);
//		}

		cartRepository.save(cart);
	}

	public void clearCart(UUID cartId){

		var cart = cartRepository.getCartWithItems(cartId).orElse(null);
		if (cart == null) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//					Map.of("error", "Cart was not found.")
//			);
			throw new CartNotFoundException();
		}

		cart.clear();

		cartRepository.save(cart);
	}
}
