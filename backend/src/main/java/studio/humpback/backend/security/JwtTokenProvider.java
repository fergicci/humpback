package studio.humpback.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_AUTHORITIES = "authorities";
    private static final String CLAIM_AUTHORITY = "authority";
    private static final String CLAIM_PURPOSE = "purpose";
    private static final String PURPOSE_2FA_LOGIN = "2fa_login";
    private static final String PURPOSE_2FA_FORGOT_PASSWORD = "2fa_forgot_password";
    private static final String ERR_INVALID_CHALLENGE_TOKEN = "Invalid challenge token";
    private static final String AUTHORITY_KEY = "authority";
    private static final String AUTHORITY_EQUALS_PREFIX = AUTHORITY_KEY + "=";
    private static final String AUTHORITY_COLON_PREFIX = AUTHORITY_KEY + ":";
    private static final char AUTHORITY_EQUALS_DELIMITER = '=';
    private static final char AUTHORITY_COLON_DELIMITER = ':';
    private static final String ROLE_LIST_SEPARATOR = ",";
    private static final String EMPTY = "";
    private static final String WRAP_SQUARE_LEFT = "[";
    private static final String WRAP_SQUARE_RIGHT = "]";
    private static final String WRAP_CURLY_LEFT = "{";
    private static final String WRAP_CURLY_RIGHT = "}";
    private static final String WRAP_DOUBLE_QUOTE = "\"";
    private static final String WRAP_SINGLE_QUOTE = "'";
    private static final String AUTHORITY_JSON_REGEX = "\"" + AUTHORITY_KEY + "\"\\s*:\\s*\"([^\"]+)\"";
    private static final Pattern AUTHORITY_JSON_PATTERN = Pattern.compile(AUTHORITY_JSON_REGEX);

    @Value("${jwt.secret}")
    private String secretKeyBase64;

    @Value("${jwt.expiration-ms}")
    private Long validityInMilliseconds;

    @Value("${jwt.clock-skew-seconds:30}")
    private long clockSkewSeconds;

    @Value("${jwt.2fa-challenge-expiration-ms:300000}")
    private Long twoFactorChallengeValidityInMilliseconds;

    @Value("${jwt.forgot-password-challenge-expiration-ms:300000}")
    private Long forgotPasswordChallengeValidityInMilliseconds;

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
                .claim(CLAIM_ROLES, roleNames)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String createTwoFactorChallengeToken(String username) {
        return createPurposeToken(username, PURPOSE_2FA_LOGIN, twoFactorChallengeValidityInMilliseconds);
    }

    public String createForgotPasswordChallengeToken(String username) {
        return createPurposeToken(username, PURPOSE_2FA_FORGOT_PASSWORD, forgotPasswordChallengeValidityInMilliseconds);
    }

    private String createPurposeToken(String username, String purpose, Long validityMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);

        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_PURPOSE, purpose)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromTwoFactorChallengeToken(String token) {
        return getUsernameFromPurposeToken(token, PURPOSE_2FA_LOGIN);
    }

    public String getUsernameFromForgotPasswordChallengeToken(String token) {
        return getUsernameFromPurposeToken(token, PURPOSE_2FA_FORGOT_PASSWORD);
    }

    private String getUsernameFromPurposeToken(String token, String expectedPurpose) {
        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(clockSkewSeconds)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object purpose = claims.get(CLAIM_PURPOSE);
            if (!expectedPurpose.equals(purpose)) {
                throw new IllegalArgumentException(ERR_INVALID_CHALLENGE_TOKEN);
            }

            return claims.getSubject();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SecurityException
                | IllegalArgumentException e) {
            throw new IllegalArgumentException(ERR_INVALID_CHALLENGE_TOKEN);
        }
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

        Object raw = resolveRoleClaim(claims);

        if (raw instanceof String s) {
            return Arrays.stream(s.split(ROLE_LIST_SEPARATOR))
                    .map(this::normalizeRoleToken)
                    .map(String::trim)
                    .filter(x -> !x.isBlank())
                    .toList();
        }

        if (raw instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .map(this::normalizeRoleToken)
                    .map(String::trim)
                    .filter(x -> !x.isBlank())
                    .toList();
        }

        return List.of();
    }

    private String normalizeRoleToken(String roleToken) {
        String normalized = Optional.ofNullable(roleToken)
                .map(String::trim)
                .orElse(EMPTY);

        if (normalized.isBlank()) {
            return EMPTY;
        }

        // Trim wrappers often seen in legacy serialized authorities.
        normalized = trimWrappers(normalized);

        Matcher jsonMatcher = AUTHORITY_JSON_PATTERN.matcher(normalized);
        if (jsonMatcher.find()) {
            normalized = jsonMatcher.group(1);
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.startsWith(AUTHORITY_EQUALS_PREFIX) || lower.startsWith(AUTHORITY_COLON_PREFIX)) {
            normalized = normalized.substring(normalized.indexOf(
                    lower.startsWith(AUTHORITY_EQUALS_PREFIX)
                            ? AUTHORITY_EQUALS_DELIMITER
                            : AUTHORITY_COLON_DELIMITER) + 1);
        }

        normalized = trimWrappers(normalized);
        return normalized.trim();
    }

    private String trimWrappers(String value) {
        String normalized = Optional.ofNullable(value).orElse(EMPTY);

        while (!normalized.isBlank()
                && (normalized.startsWith(WRAP_SQUARE_LEFT)
                || normalized.startsWith(WRAP_CURLY_LEFT)
                || normalized.startsWith(WRAP_DOUBLE_QUOTE)
                || normalized.startsWith(WRAP_SINGLE_QUOTE))) {
            normalized = normalized.substring(1).trim();
        }

        while (!normalized.isBlank()
                && (normalized.endsWith(WRAP_SQUARE_RIGHT)
                || normalized.endsWith(WRAP_CURLY_RIGHT)
                || normalized.endsWith(WRAP_DOUBLE_QUOTE)
                || normalized.endsWith(WRAP_SINGLE_QUOTE))) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }

        return normalized;
    }

    private Object resolveRoleClaim(io.jsonwebtoken.Claims claims) {
        return Optional.ofNullable(claims.get(CLAIM_ROLES))
                .or(() -> Optional.ofNullable(claims.get(CLAIM_ROLE))) // backward compatibility with singular claim
                .or(() -> Optional.ofNullable(claims.get(CLAIM_AUTHORITIES))) // Spring Security defaults
                .or(() -> Optional.ofNullable(claims.get(CLAIM_AUTHORITY))) // backward compatibility with singular authority
                .orElse(null);
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
