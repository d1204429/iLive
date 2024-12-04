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

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  private JwtUtil jwtUtil;

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()  // 限制GET
            .requestMatchers("/api/v1/admin/products/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/users/{id}").authenticated()  // 添加具體的用戶訪問權限
            .anyRequest().authenticated()
        )

        // 添加 JWT 過濾器
        .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
            UsernamePasswordAuthenticationFilter.class);  // 添加這一行

    return http.build();
  }
}


//            .requestMatchers("/api/v1/users/**").permitAll()
