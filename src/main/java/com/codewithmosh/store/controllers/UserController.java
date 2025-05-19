package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.ChangePasswordRequest;
import com.codewithmosh.store.dtos.RegisterUserRequest;
import com.codewithmosh.store.dtos.UpdateUserRequest;
import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.entities.Role;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@GetMapping
	//Method: GET, POST, PUT, DELETE

	public Iterable<UserDto> getAllUsers(
		@RequestHeader(required = false, name = "x-auth-token") String authToken,
		@RequestParam(required = false, defaultValue = "", name = "sort") String sort){

	if (!Set.of("name","email").contains(sort)) {
			sort = "name";
		}
		return userRepository.findAll()
				.stream()
				//.map( user -> new UserDto(user.getId(), user.getName(), user.getEmail()))
				//.map( user -> userMapper.toDto(user))
				.map(userMapper::toDto)
				.toList();
	}

//	//@RequestMapping("/users")
//	@GetMapping("/{id}")
//	//Method: GET, POST, PUT, DELETE
//	public User getUser(@PathVariable Long id){
//		return userRepository.findById(id).orElse(null);
//	}

	//@RequestMapping("/users")
	@GetMapping("/{id}")
	//Method: GET, POST, PUT, DELETE
	public ResponseEntity<UserDto> getUser(@PathVariable Long id){
		var user = userRepository.findById(id).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}
		//return ResponseEntity.ok(user);
//		var userDto = new UserDto(user.getId(), user.getName(), user.getEmail());
//		return ResponseEntity.ok(userDto);
		return ResponseEntity.ok(userMapper.toDto(user));
	}

//	//@RequestMapping("/users")
//	@GetMapping("/users")
//	//Method: GET, POST, PUT, DELETE
//	public Iterable<User> getAllUsers(){
//		return userRepository.findAll();
//	}
//
//	//@RequestMapping("/users")
//	@GetMapping("/user/{id}")
//	//Method: GET, POST, PUT, DELETE
//	public User getUser(@PathVariable Long id){
//		return userRepository.findById(id).orElse(null);
//	}

	@PostMapping
	//MethodArgumentNotValidException we have to handle this exception with Exception handler
	public ResponseEntity<?> registerUser/*createUser*/(
			//@RequestBody UserDto data,
			@Valid @RequestBody RegisterUserRequest request,
			UriComponentsBuilder uriBuilder
			) {

		//Business logic
		if (userRepository.existsByEmail(request.getEmail())) {
			return ResponseEntity.badRequest().body(
					Map.of("email", "Email is already registered.")
			);
		}



		var user = userMapper.toEntity(request);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole(Role.USER);  //we can do it in mapper
		userRepository.save(user);

		var userDto = userMapper.toDto(user);
		//return userDto;
		var uri = uriBuilder.path("/users/{id}").buildAndExpand(userDto.getId()).toUri();

		return ResponseEntity.created(uri).body(userDto);


	}

	@PutMapping("/{id}")
	public ResponseEntity<UserDto> updateUser(
			@PathVariable(name = "id") Long id,
			@Valid @RequestBody UpdateUserRequest request
			// userDto
		) {

		var user = userRepository.findById(id).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}
//		//manual mapping only for small objects
//		user.setName(request.getName());
//		user.setEmail(request.getEmail());
//		userRepository.save(user);

		userMapper.update(request, user);
		userRepository.save(user);
		return ResponseEntity.ok(userMapper.toDto(user));

	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id){
		var user = userRepository.findById(id).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}
		userRepository.delete(user);
		return ResponseEntity.noContent().build();
	}

	//TODO we should avoid saving the password as plain text
	// will be discussed later in security section
	@PostMapping("/{id}/change-password")
	public ResponseEntity<Void> changePassword(
			@PathVariable Long id,
			@RequestBody ChangePasswordRequest request){
		var user = userRepository.findById(id).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}
		if (!user.getPassword().equals(request.getOldPassword())) {
			//return ResponseEntity.badRequest().build();
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); //not authorized
		}

		user.setPassword(request.getNewPassword());
		userRepository.save(user);

		return ResponseEntity.noContent().build();
	}


}
