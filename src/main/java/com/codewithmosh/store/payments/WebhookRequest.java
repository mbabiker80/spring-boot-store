package com.codewithmosh.store.payments;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class WebhookRequest {
	public Map<String, String> headers;
	public String payload;
}
