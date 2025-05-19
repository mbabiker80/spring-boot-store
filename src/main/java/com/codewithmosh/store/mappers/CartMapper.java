package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.CartDto;
import com.codewithmosh.store.dtos.CartItemDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
	//@Mapping(target = "items", source = "cartItems")
	//@Mapping(target = "items", source = "items")
	//We dont need the mapping when source and target have the same name
	@Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
	CartDto toDto(Cart cart);

	//doesnt match the name of the field
	//expression instead of source, cartItem is a Methodparameter
	@Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
	CartItemDto toDto(CartItem cartItem);

}
