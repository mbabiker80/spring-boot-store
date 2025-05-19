package com.codewithmosh.store.controllers;

import com.codewithmosh.store.config.JwtConfig;
import com.codewithmosh.store.dtos.JwtResponse;
import com.codewithmosh.store.dtos.LoginRequest;
//import com.codewithmosh.store.repositories.UserRepository;
import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.UserRepository;
import com.codewithmosh.store.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
//import org.springframework.http.HttpStatus;
import org.antlr.v4.runtime.atn.RangeTransition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

	//private final UserRepository userRepository;
	//private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final JwtConfig jwtConfig;
	private final UserRepository userRepository;
	private final UserMapper userMapper;

	@PostMapping("/login")
	public ResponseEntity<JwtResponse> login(
			@Valid @RequestBody LoginRequest request, HttpServletResponse response){

//		var user = userRepository.findByEmail(request.getEmail()).orElse(null);
//		if (user == null) {
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//		}
//
//		if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//		}

		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()
				)
		);

		//var token = jwtService.generateToken(request.getEmail());

		var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

		var accessToken = jwtService.generateAccessToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);

		var cookie = new Cookie("refreshToken", refreshToken.toString());
		cookie.setHttpOnly(true);
		cookie.setPath("/auth/refresh"); //  /entire website  //auth/refresh to reduce attack sirface
		cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration()); // 604800 7d   (60 * 60 * 24 * 30) = 30 days
		cookie.setSecure(true); // can only be sent over https connections, to prevent beeing unincrepted over http channels
		response.addCookie(cookie);

		return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
	}

	/*
	public boolean validate(@RequestHeader("Authorization") String authHeader){

		System.out.println("Validate called");

		//service expects a token not a requestHeader
		//var token = authHeader.substring(7);
		var token = authHeader.replace("Bearer ", "");

		return jwtService.validateToken(token);

	}
	*/

	@PostMapping("/refresh")
	public ResponseEntity<JwtResponse> refresh(
			@CookieValue(value = "refreshToken") String refreshToken
	){
		var jwt = jwtService.parseToken(refreshToken);

		//if (!jwtService.validateToken(refreshToken)) {
		if (jwt == null || jwt.isExpired()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		//var userId = jwt.getUserId(); // jwtService.getUserIdFromToken(refreshToken);

		var user = userRepository.findById(jwt.getUserId()).orElseThrow();
		var accessToken = jwtService.generateAccessToken(user);

		return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
	}

	@GetMapping("/me")
	public ResponseEntity<UserDto> me(){
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		//var email = (String)authentication.getPrincipal();
		//var user = userRepository.findByEmail(email).orElse(null);

		var userId = (Long)authentication.getPrincipal();

		var user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}

		var userDto = userMapper.toDto(user);
		return ResponseEntity.ok(userDto);

	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Void> handleBadCredentials(){
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

	}
}
