package fcu.iLive.config;

import fcu.iLive.filter.JwtAuthenticationFilter;
import fcu.iLive.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

  @Autowired
  private JwtUtil jwtUtil;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // 靜態資源和基礎路徑
                    .requestMatchers(
                            "/",
                            "/static/**",
                            "/images/**",
                            "/favicon.ico",
                            "/error",
                            "/products/**",
                            "/categories/**"
                    ).permitAll()
                    // 公開API路徑
                    .requestMatchers(
                            "/users/register",
                            "/users/login",
                            "/users/refresh-token",
                            "/users/logout"
                    ).permitAll()
                    // GET請求公開路徑
                    .requestMatchers(HttpMethod.GET,
                            "/products/**",
                            "/categories/**",
                            "/promotions/**"
                    ).permitAll()
                    // 管理員路徑
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    // 需要認證的路徑
                    .requestMatchers("/users/{userId}/**").authenticated()
                    .requestMatchers("/cart/**").authenticated()
                    .requestMatchers("/orders/**").authenticated()
                    // 其他請求允許訪問
                    .anyRequest().permitAll()
            )
            .addFilterBefore(
                    new JwtAuthenticationFilter(jwtUtil),
                    UsernamePasswordAuthenticationFilter.class
            )
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                      logger.error("Unauthorized error: {}", authException.getMessage());
                      response.setStatus(401);
                      response.setContentType("application/json;charset=UTF-8");
                      response.getWriter().write("{\"message\":\"未授權訪問\"}");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                      logger.error("Access denied error: {}", accessDeniedException.getMessage());
                      response.setStatus(403);
                      response.setContentType("application/json;charset=UTF-8");
                      response.getWriter().write("{\"message\":\"拒絕訪問\"}");
                    })
            );

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8080",
            "http://192.168.226.1:8080",
            "http://192.168.180.1:8080",
            "http://192.168.43.90:8080"
    ));
    configuration.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS",
            "PATCH"
    ));
    configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Cache-Control"
    ));
    configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers"
    ));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
