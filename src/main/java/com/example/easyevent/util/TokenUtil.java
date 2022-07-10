package com.example.easyevent.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class TokenUtil {
    static final String ISSUER = "Ziwei_Cao";
    static final String USER_ID = "userId";
    static final long MILL_SECONDS_IN_HOUR = 1*60*60*1000;
    static Algorithm algorithm = Algorithm.HMAC256("mysecretkey");

    public static String signToken(Integer userId, int expirationInHour){
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withClaim(USER_ID, userId)
                .withExpiresAt(new Date(System.currentTimeMillis()+expirationInHour*MILL_SECONDS_IN_HOUR))
                .sign(algorithm);
        return token;
    }
    public static Integer verifyToken(String token){
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        Integer userId = jwt.getClaim(USER_ID).asInt();
        return userId;
    }
}
