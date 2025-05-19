package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.OrderDto;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.mappers.OrderMapper;
import com.codewithmosh.store.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final AuthService authService;
	private final OrderMapper orderMapper;

	public List<OrderDto> getAllOrders(){

		var user = authService.getCurrentUser();
		//var orders = orderRepository.findAllByCustomer(user);
		var orders = orderRepository.getOrdersByCustomer(user);
		return orders.stream().map(orderMapper::toDto).toList();
	}

	public OrderDto getOrder(Long orderId) {
		var order = orderRepository.getOrderWithItems(orderId).orElseThrow(OrderNotFoundException::new);
//		if (order == null) {
//			throw new OrderNotFoundException();
//		}

		var user = authService.getCurrentUser();
		//violation of the information expert principal, order customer id longobject / who have the data should do the job
		//if (!order.getCustomer().getId().equals(user.getId())) {
		if (!order.isPlacedBy(user)) {
			throw new AccessDeniedException("you don't have access to this order");
		}
		return orderMapper.toDto(order);
	}
}
