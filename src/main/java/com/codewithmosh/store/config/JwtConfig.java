package com.codewithmosh.store.config;


import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt") //where are settings located in our config file
@Data
public class JwtConfig {
	private String secret;
	private int accessTokenExpiration;
	private int refreshTokenExpiration;

	public SecretKey getSecretKey(){
		return Keys.hmacShaKeyFor(secret.getBytes());
	}
}
