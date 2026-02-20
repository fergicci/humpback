package studio.humpback.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsername(token);
            List<String> roles = jwtTokenProvider.getRoles(token);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    mapAuthorities(roles)
            );

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private List<SimpleGrantedAuthority> mapAuthorities(List<String> roles) {
        Set<String> normalizedAuthorities = new LinkedHashSet<>();

        for (String role : roles) {
            if (role == null) {
                continue;
            }

            String trimmed = role.trim();
            if (trimmed.isBlank()) {
                continue;
            }

            String upper = trimmed.toUpperCase(Locale.ROOT);
            String rawRole = upper.startsWith(ROLE_PREFIX)
                    ? upper.substring(ROLE_PREFIX.length())
                    : upper;

            if (rawRole.isBlank()) {
                continue;
            }

            normalizedAuthorities.add(rawRole);
            normalizedAuthorities.add(ROLE_PREFIX + rawRole);
        }

        return normalizedAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
