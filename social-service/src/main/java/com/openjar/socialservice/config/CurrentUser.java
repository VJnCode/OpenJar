package com.openjar.socialservice.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

/** Identity extracted from the access token issued by user-service. */
public record CurrentUser(String userId, String userName) {

    public static CurrentUser from(Jwt jwt) {
        String uid = jwt == null ? null : jwt.getClaimAsString("uid");
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has no user id");
        }
        String name = jwt.getClaimAsString("name");
        return new CurrentUser(uid, name != null ? name : "OpenJar cook");
    }

    /** For endpoints that are public but personalise the response when logged in. */
    public static String userIdOrNull(Jwt jwt) {
        return jwt == null ? null : jwt.getClaimAsString("uid");
    }
}
