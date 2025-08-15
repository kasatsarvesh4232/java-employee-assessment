package com.reliaquest.api.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

@Slf4j
public class RateLimitingHandler {

    public static <T> T retryOnRateLimit(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (HttpClientErrorException.TooManyRequests ex) {
            HttpHeaders headers = ex.getResponseHeaders();
            String retryAfter = headers != null ? headers.getFirst("Retry-After") : null;

            String message = "Rate limit exceeded.";
            if (retryAfter != null) {
                message += " Retry after " + retryAfter + " seconds.";
                log.warn("Rate limit hit. Retry-After: {} seconds", retryAfter);
            } else {
                log.warn("Rate limit hit. No Retry-After header provided.");
            }

            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
        } catch (HttpClientErrorException ex) {
            log.error("HTTP error occurred while calling external API: {}", ex.getStatusCode(), ex);
            throw new RuntimeException("HTTP error occurred: " + ex.getStatusCode(), ex);
        }
    }
}
