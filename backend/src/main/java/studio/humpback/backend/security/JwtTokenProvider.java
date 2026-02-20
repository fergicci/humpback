package studio.humpback.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import studio.humpback.backend.model.UserRole;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyBase64;

    @Value("${jwt.expiration-ms}")
    private Long validityInMilliseconds;

    @Value("${jwt.clock-skew-seconds:30}")
    private long clockSkewSeconds;

    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys
                .hmacShaKeyFor(secretKeyBase64
                        .getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String username, Set<UserRole> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);

        List<String> roleNames = roles.stream()
                .map(Enum::name)
                .toList();

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roleNames)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public List<String> getRoles(String token) {
        var claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object raw = claims.get("roles");
        if (raw == null)
            return List.of();

        if (raw instanceof String s) {
            return Arrays.stream(s.split(","))
                    .map(String::trim)
                    .filter(x -> !x.isBlank())
                    .toList();
        }

        if (raw instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(x -> !x.isBlank())
                    .toList();
        }

        return List.of();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(clockSkewSeconds)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            return false;
        }
    }
}
