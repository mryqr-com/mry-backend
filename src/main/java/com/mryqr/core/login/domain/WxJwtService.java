package com.mryqr.core.login.domain;

import com.mryqr.common.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class WxJwtService {
    private static final String MOBILE_OPEN_ID = "mo";
    private static final String PC_OPEN_ID = "po";
    private final JwtProperties jwtProperties;

    public String generateMobileWxIdInfoJwt(String unionId, String mobileWxOpenId) {
        return generateWxIdInfoJwt(unionId, mobileWxOpenId, null);
    }

    public String generatePcWxIdInfoJwt(String unionId, String pcWxOpenId) {
        return generateWxIdInfoJwt(unionId, null, pcWxOpenId);
    }

    private String generateWxIdInfoJwt(String unionId, String mobileWxOpenId, String pcWxOpenId) {
        Claims claims = Jwts.claims().setSubject(unionId);

        if (isNotBlank(mobileWxOpenId)) {
            claims.put(MOBILE_OPEN_ID, mobileWxOpenId);
        }

        if (isNotBlank(pcWxOpenId)) {
            claims.put(PC_OPEN_ID, pcWxOpenId);
        }

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + 30 * 60L * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(HS256, jwtProperties.getSecret())
                .compact();
    }

    public WxIdInfo wxIdInfoFromJwt(String jwt) {
        if (isBlank(jwt)) {
            return null;
        }

        try {
            Claims claims = Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(jwt).getBody();
            String wxUnionId = claims.getSubject();
            String mobileWxOpenId = claims.get(MOBILE_OPEN_ID, String.class);
            String pcWxOpenId = claims.get(PC_OPEN_ID, String.class);
            return WxIdInfo.builder()
                    .wxUnionId(wxUnionId)
                    .mobileWxOpenId(mobileWxOpenId)
                    .pcWxOpenId(pcWxOpenId)
                    .build();
        } catch (Throwable t) {
            return null;
        }
    }


}
