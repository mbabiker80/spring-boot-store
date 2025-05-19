package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "carts")
public class Cart {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	//generatedValue is only for primary key
	@Column(name = "id")
	private UUID id;

	@Column(name = "date_created", insertable = false, updatable = false)
	//ignore this field by generating sql statement
	private LocalDate dateCreated;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.MERGE,
			fetch = FetchType.EAGER, orphanRemoval = true)
	//to persist cardItem whene we save cart
	//fetchstrategy by oneToMany is always lazy, we should change it for siplifying the sql Query
	//catr, cartitem, producht -- category (all in single session)
	//an item can not be null. when we remove the item we should allow orphanremoval
	private Set<CartItem> items = new LinkedHashSet<>();

	public BigDecimal getTotalPrice() {
/*
		BigDecimal total = BigDecimal.ZERO;

		for (CartItem item : items) {
			total = total.add(item.getTotalPrice());
		}
		*/

		return items.stream()
				.map(CartItem::getTotalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		//reduce to a single value

	}

	public CartItem getItem(Long productId) {

		return items.stream()
				.filter(item -> item.getProduct().getId().equals(productId))
				.findFirst()
				.orElse(null);
	}

	public CartItem addItem(Product product) {
		var cartItem = getItem(product.getId());

		if (cartItem != null) {
			cartItem.setQuantity(cartItem.getQuantity() + 1);
		} else {
			cartItem = new CartItem();
			cartItem.setProduct(product);
			cartItem.setQuantity(1);
			cartItem.setCart(this);
			getItems().add(cartItem);
		}
		return cartItem;
	}

	public void removeItem(Long productId) {
		var cartItem = getItem(productId);
		if (cartItem != null) {
			getItems().remove(cartItem);
		}
	}

	public void clear() {
		getItems().clear();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

}