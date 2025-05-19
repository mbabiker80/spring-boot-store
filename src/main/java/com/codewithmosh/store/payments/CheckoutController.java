package com.codewithmosh.store.payments;

import com.codewithmosh.store.dtos.ErrorDto;
import com.codewithmosh.store.exceptions.CartEmptyException;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.repositories.OrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/checkout")
public class CheckoutController {

//	private final CartRepository cartRepository;
//	private final AuthService authService;
//	private final OrderRepository orderRepository;
//	private final CartService cartService;
	private final CheckoutService checkoutService;
	private final OrderRepository orderRepository;

	@PostMapping
	public CheckoutResponse checkout( //ResponseEntity<CheckoutResponse> //ResponseEntity<?> //CheckoutResponse
			@Valid @RequestBody CheckoutRequest request
	) {

//		try {
			//return ResponseEntity.ok(checkoutService.checkout(request));
			return checkoutService.checkout(request);

//		} catch (PaymentException e) { //StripeException e
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//					new ErrorDto("Error creating a checkout session")
//			);
//			//throw new RuntimeException(e);
//		}

		/*
		//cartRepository.findById(request.getCartId())
		var cart = cartRepository.getCartWithItems(request.getCartId()).orElse(null);
		if (cart == null) {
			return ResponseEntity.badRequest().body( //instead of not found
					new ErrorDto("Cart not found.")
					//Map.of("error", "Cart not found")
			);
		}

		if (cart.getItems().isEmpty()) {
			return ResponseEntity.badRequest().body(
					new ErrorDto("Cart is empty")
					//Map.of("error", "Cart is empty")
			);
		}


//		var order = new Order();
//
//		order.setTotalPrice(cart.getTotalPrice());
//		order.setStatus(OrderStatus.PENDING);
//		order.setCustomer(authService.getCurrentUser());
//
//		cart.getItems().forEach(item -> {
//			var orderItem = new OrderItem();
//			orderItem.setOrder(order);
//			orderItem.setProduct(item.getProduct());
//			orderItem.setQuantity(item.getQuantity());
//			orderItem.setTotalPrice(item.getTotalPrice());
//			orderItem.setUnitPrice(item.getProduct().getPrice());
//			order.getItems().add(orderItem);
//		});

		var order = Order.fromCart(cart, authService.getCurrentUser());

		orderRepository.save(order);

		cartService.clearCart(cart.getId());

		return ResponseEntity.ok(new CheckoutResponse(order.getId()));

		*/

	}

	@PostMapping("/webhook")
	public void handleWebhook(
			@RequestHeader Map<String, String> headers,
			@RequestBody String payload
	){
				checkoutService.handleWebhookEvent(new WebhookRequest(headers, payload));


	}

	@ExceptionHandler(PaymentException.class)
	public ResponseEntity<ErrorDto> handlePaymentException(PaymentException exception){
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				new ErrorDto("Error creating a checkout session")
		);
	}

	@ExceptionHandler({CartNotFoundException.class, CartEmptyException.class})
	public ResponseEntity<ErrorDto> handleException(Exception exception){
		return ResponseEntity.badRequest().body(
				new ErrorDto(exception.getMessage())
		);

	}
}
