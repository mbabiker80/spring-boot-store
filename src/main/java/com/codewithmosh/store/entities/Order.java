package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private User customer;

	@Column(name = "status")
	@Enumerated(EnumType.STRING) //EnumType.ORDINAL : the ordinal value like 0,1, or 2 will be stored in the DB
	//
	private PaymentStatus status;

	@Column(name = "created_at", insertable = false, updatable = false) // DB will take care of assigning the value
	private LocalDateTime createdAt;

	@Column(name = "total_price")
	private BigDecimal totalPrice;

	@OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private Set<OrderItem> items = new LinkedHashSet<>();

	public static Order fromCart(Cart cart, User customer) {

		var order = new Order();
		order.setCustomer(customer);
		order.setStatus(PaymentStatus.PENDING);
		order.setTotalPrice(cart.getTotalPrice());

		cart.getItems().forEach(item -> {
			var orderItem = new OrderItem(order, item.getProduct(), item.getQuantity());
			/*var orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setProduct(item.getProduct());
			orderItem.setQuantity(item.getQuantity());
			orderItem.setTotalPrice(item.getTotalPrice());
			orderItem.setUnitPrice(item.getProduct().getPrice());
			order.getItems().add(orderItem);*/
			order.items.add(orderItem);
		});

		return order;
	}

	public boolean isPlacedBy(User customer) {
		//return this.customer.getId().equals(customer.getId());
		return this.customer.equals(customer);
	}

}