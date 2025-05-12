package com.mryqr.common.security.jwt;

import com.mryqr.common.security.MryAuthenticationToken;
import com.mryqr.core.common.properties.JwtProperties;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.mryqr.core.common.domain.user.User.humanUser;
import static io.jsonwebtoken.SignatureAlgorithm.HS512;

@Component
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;

    public String generateJwt(String memberId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtProperties.getExpire() * 60L * 1000L);
        return generateJwt(memberId, expirationDate);
    }

    public String generateJwt(String memberId, Date expirationDate) {
        Claims claims = Jwts.claims().setSubject(memberId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(HS512, jwtProperties.getSecret())
                .compact();
    }

    public MryAuthenticationToken tokenFrom(String jwt) {
        Claims claims = Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(jwt).getBody();
        String memberId = claims.getSubject();
        Member member = memberRepository.cachedById(memberId);
        member.checkActive();
        long expiration = claims.getExpiration().getTime();
        return new MryAuthenticationToken(humanUser(memberId, member.getName(), member.getTenantId(), member.getRole()), expiration);
    }

}
