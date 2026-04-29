package com.iwos.testkit;

/**
 * HTTP client for API testing.
 * Provides utilities for making REST calls in tests.
 */
public class ApiTestClient {

    private String baseUrl = "http://localhost:8080";
    private int timeout = 5000;

    public ApiTestClient withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public ApiTestClient withTimeout(int timeoutMs) {
        this.timeout = timeoutMs;
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    /**
     * Execute GET request
     */
    public ApiResponse get(String path) {
        // Placeholder for actual implementation
        return new ApiResponse(200, "{}");
    }

    /**
     * Execute POST request
     */
    public ApiResponse post(String path, String body) {
        // Placeholder for actual implementation
        return new ApiResponse(200, "{}");
    }

    /**
     * Response wrapper
     */
    public static class ApiResponse {
        private final int statusCode;
        private final String body;

        public ApiResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
