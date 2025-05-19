package com.codewithmosh.store.services;

import com.codewithmosh.store.config.JwtConfig;
import com.codewithmosh.store.entities.Role;
import com.codewithmosh.store.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class JwtService {

	//@Value("${spring.jwt.secret}")
	//private String secret;

	private final JwtConfig jwtConfig;

/*
	//public String generateToken(String email) {
	public String generateAccessToken(User user) {
		//final long tokenExpiration = 300; // 5 Minutes //86400;  // 1 day (seconds

		return generateToken(user, jwtConfig.getAccessTokenExpiration() );

		//return "token";
	}

	public String generateRefreshToken(User user) {
		//final long tokenExpiration = 60800; // 7 day
		return generateToken(user, jwtConfig.getRefreshTokenExpiration());
	} */

	//public String generateToken(String email) {
	public Jwt generateAccessToken(User user) {

		return generateToken(user, jwtConfig.getAccessTokenExpiration() );
	}

	public Jwt generateRefreshToken(User user) {
		return generateToken(user, jwtConfig.getRefreshTokenExpiration());
	}

	//private String generateToken(User user, long tokenExpiration) {
	private Jwt generateToken(User user, long tokenExpiration) {
		var claims = Jwts.claims()
				.subject(user.getId().toString())
				.add("email", user.getEmail())
				.add("name", user.getName())
				.add("role", user.getRole())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
				.build();


		return new Jwt(claims, jwtConfig.getSecretKey());
/*
		return Jwts.builder()
				//.subject(email)
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim("name", user.getName())
				.claim("role", user.getRole())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
				//dont store password in text, should put it somewhere save, e.x application yml file
				//.signWith(Keys.hmacShaKeyFor(secret.getBytes())) //string is 6 char long which is not secure enough
				//.signWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
				.signWith(jwtConfig.getSecretKey())
				.compact();
		*/


	}


	/*
	public boolean validateToken(String token) {
		try{
			var claims = getClaims(token);

			return claims.getExpiration().after(new Date());

		}
		catch (Exception e){
			return false;
		}
	}
	*/

	private Claims getClaims(String token) {
		return Jwts.parser()
				//.verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
				.verifyWith(jwtConfig.getSecretKey())
				.build()
				.parseClaimsJws(token)
				.getPayload();
	}

	//moved to Jws-Class
	/*
	public String getEmailFromToken(String token) {
		return getClaims(token).getSubject();
	}

	public Long getUserIdFromToken(String token) {
		return Long.valueOf(getClaims(token).getSubject());
	}

	public Role getRoleFromToken(String token) {
		return Role.valueOf(getClaims(token).get("role", String.class));
	}
	*/

	public Jwt parseToken(String token) {
		try {
			var claims = getClaims(token);
			return new Jwt(claims, jwtConfig.getSecretKey());
		}
		catch (JwtException e) {
			return null;
		}
	}
}
