package com.codewithmosh.store.payments;

import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.OrderItem;
import com.codewithmosh.store.entities.PaymentStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class StripePaymentGateway implements PaymentGateway {


	@Value("${websiteUrl}")//http:localhost:4242
	private String websiteUrl;

	@Value("${stripe.webhookSecretKey}")
	private String webhookSecretKey;

	@Override
	public CheckoutSession createCheckoutSession(Order order) {

		try {

			//Create a checkout Session
			var builder = SessionCreateParams.builder()
					.setMode(SessionCreateParams.Mode.PAYMENT)  //or SUBSCRIPTIONS
					.setSuccessUrl(websiteUrl + "/checkout-success?orderId=" + order.getId()) //"http://localhost:3000/success.html"
					.setCancelUrl(websiteUrl + "/checkout-cancel") //"http://localhost:3000/cancel.html"
					.putMetadata("order_id", order.getId().toString());


			order.getItems().forEach(item -> {

				var lineItem = createLineItem(item);
				builder.addLineItem(lineItem);

			});

			var session = Session.create(builder.build());

			return new CheckoutSession(session.getUrl());
		}
		catch (StripeException exception){
			System.out.println(exception.getMessage());
			throw new PaymentException();
		}
	}

	@Override
	public Optional<PaymentResult> parseWebhookRequest(WebhookRequest request) {
		try {
			// WebhookRequest -> {orderId, paymentStatus} (PaymentResult)
			var payload = request.getPayload();
			var signature = request.getHeaders().get("stripe-signature");

			var event = Webhook.constructEvent(payload, signature, webhookSecretKey);
			//System.out.println(event.getType());


			//charge -> (Charge) stripeObject;
			// payment_intent.succeeded -> (PaymentIntent) stripeObject;

			return switch (event.getType()) {
				// Update order status (PAID)
				case "payment_intent.succeeded" ->
						Optional.of( new PaymentResult(extractOrderId(event), PaymentStatus.PAID));
					//var paymentIntent = (PaymentIntent)stripeObject;
					//if (paymentIntent != null) {
						//System.out.println("Payment intent: " + paymentIntent.getId());
						//var orderId = paymentIntent.getMetadata().get("order_id");

					//	return Optional.of( new PaymentResult(Long.valueOf(orderId), PaymentStatus.PAID));
					//}



				// Update order status (FAILED)
				case "payment_intent.payment_failed" ->
					Optional.of( new PaymentResult(extractOrderId(event), PaymentStatus.FAILED));

				default -> Optional.empty();

			};

			//return ResponseEntity.ok().build();

//			return Optional.empty();
			//			if (stripeObject == null) {
			//				return ResponseEntity.badRequest().build();
			//			}

		} catch (SignatureVerificationException e) {
			//return ResponseEntity.badRequest().build();
			throw new PaymentException("Invalid signature");
		}

	}

	private Long extractOrderId(Event event) {
		//var stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
		var stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
				() -> new PaymentException("Could not deserialize Stripe event. check the SDK and API version.")
		);

		var paymentIntent = (PaymentIntent)stripeObject;
		//if (paymentIntent != null) {
			//System.out.println("Payment intent: " + paymentIntent.getId());
		//	var orderId = paymentIntent.getMetadata().get("order_id");
		//}
		return Long.valueOf(paymentIntent.getMetadata().get("order_id"));
	}

	private SessionCreateParams.LineItem createLineItem(OrderItem item) {
		return SessionCreateParams.LineItem.builder()
				.setQuantity(Long.valueOf(item.getQuantity()))
				.setPriceData(createPriceData(item))
				.build();
	}

	private SessionCreateParams.LineItem.PriceData createPriceData(OrderItem item) {
		return SessionCreateParams.LineItem.PriceData.builder()
				.setCurrency("usd")
				.setUnitAmountDecimal(
						item.getUnitPrice().multiply(BigDecimal.valueOf(100)))
				.setProductData(createProductData(item))
				.build();
	}

	private SessionCreateParams.LineItem.PriceData.ProductData createProductData(OrderItem item) {
		return SessionCreateParams.LineItem.PriceData.ProductData.builder()
				.setName(item.getProduct().getName())
				//.setDescription() //optional
				.build();
	}
}
