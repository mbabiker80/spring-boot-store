package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.ErrorDto;
import com.codewithmosh.store.dtos.OrderDto;
//mport com.codewithmosh.store.mappers.OrderMapper;
//import com.codewithmosh.store.repositories.OrderRepository;
//import com.codewithmosh.store.services.AuthService;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.services.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

//	private final OrderRepository orderRepository;
//	private final AuthService authService;
//	private final OrderMapper orderMapper;
	private final OrderService orderService;

	@GetMapping
	public List<OrderDto> getAllOrders() {
		return orderService.getAllOrders();
	}

	@GetMapping("/{orderId}")
	public OrderDto getOrder(@PathVariable("orderId") Long orderId){
		return orderService.getOrder(orderId);
	}

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<Void> handleOrderNotFound(){
		return ResponseEntity.notFound().build();
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorDto> handleAccessDenied(AccessDeniedException exception){
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
				new ErrorDto(exception.getMessage())
		);
	}
}
