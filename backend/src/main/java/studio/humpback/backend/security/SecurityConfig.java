package studio.humpback.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String API_AUTH_LOGIN = "/api/v1/auth/login";
    private static final String API_CONTACTS = "/api/v1/contacts";
    private static final String API_BOOKINGS = "/api/v1/bookings";
    private static final String API_BOOKINGS_TODAY = "/api/v1/bookings/today";
    private static final String API_BOOKINGS_ON = "/api/v1/bookings/on";
    private static final String API_NEWS = "/api/v1/news";
    private static final String API_OPTIONS_ALL = "/**";

    private final JwtTokenFilter jwtTokenFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, API_AUTH_LOGIN).permitAll()
                        .requestMatchers(HttpMethod.POST, API_CONTACTS).permitAll()
                        .requestMatchers(HttpMethod.POST, API_BOOKINGS).permitAll()
                        .requestMatchers(HttpMethod.GET, API_BOOKINGS_TODAY).permitAll()
                        .requestMatchers(HttpMethod.GET, API_BOOKINGS_ON).permitAll()
                        .requestMatchers(HttpMethod.GET, API_NEWS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, API_OPTIONS_ALL).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
