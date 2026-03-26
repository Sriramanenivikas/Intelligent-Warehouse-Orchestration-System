package com.iwos.common.security;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "roles";
    public static final String USER_ID_KEY = "userId";
    public static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000L;  // 15 minutes
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L; // 7 days
}
