package com.codewithmosh.store.payments;

import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.PaymentStatus;
import com.codewithmosh.store.exceptions.CartEmptyException;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.services.AuthService;
import com.codewithmosh.store.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor // only fields that are defined as final will be initialized
@Service
public class CheckoutService {

	private final CartRepository cartRepository;
	private final AuthService authService;
	private final OrderRepository orderRepository;
	private final CartService cartService;
	private final PaymentGateway paymentGateway;


	@Transactional
	public CheckoutResponse checkout(CheckoutRequest request) { //throws StripeException


		//cartRepository.findById(request.getCartId())
		var cart = cartRepository.getCartWithItems(request.getCartId()).orElse(null);
		if (cart == null) {
			throw new CartNotFoundException();
//			return ResponseEntity.badRequest().body( //instead of not found
//					new ErrorDto("Cart not found.")
//					//Map.of("error", "Cart not found")
//			);
		}

		//if (cart.getItems().isEmpty()) {
		if (cart.isEmpty()) {
			throw new CartEmptyException();

//				return ResponseEntity.badRequest().body(
//						new ErrorDto("Cart is empty")
//						//Map.of("error", "Cart is empty")
//				);
		}


	/*
	var order = new Order();

	order.setTotalPrice(cart.getTotalPrice());
	order.setStatus(OrderStatus.PENDING);
	order.setCustomer(authService.getCurrentUser());

	cart.getItems().forEach(item -> {
		var orderItem = new OrderItem();
		orderItem.setOrder(order);
		orderItem.setProduct(item.getProduct());
		orderItem.setQuantity(item.getQuantity());
		orderItem.setTotalPrice(item.getTotalPrice());
		orderItem.setUnitPrice(item.getProduct().getPrice());
		order.getItems().add(orderItem);
	}); */

		var order = Order.fromCart(cart, authService.getCurrentUser());

		orderRepository.save(order);

		try {


			var session = paymentGateway.createCheckoutSession(order);


					cartService.clearCart(cart.getId());

			//return ResponseEntity.ok(new CheckoutResponse(order.getId()));

			return new CheckoutResponse(order.getId(), session.getCheckoutUrl()); // session.getUrl()

		}
		catch (PaymentException exception){ //StripeException exception
			orderRepository.delete(order);
			throw exception;
		}
	}

	public void handleWebhookEvent(WebhookRequest request){
		paymentGateway
				.parseWebhookRequest(request)
				.ifPresent(paymentResult -> {
					//var order = orderRepository.findById(Long.parseLong(orderId)).orElseThrow();
					var order = orderRepository.findById(paymentResult.getOrderId()).orElseThrow();
					order.setStatus(PaymentStatus.PAID);
					orderRepository.save(order);
				});


	}
}
