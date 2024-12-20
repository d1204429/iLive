package fcu.iLive.config;

import fcu.iLive.util.JwtUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Getter
@Configuration
public class JwtConfig {

  @Value("${security.jwt.secret}")
  private String secret;

  @Value("${jwt.expiration.access}")
  private Long accessTokenExpiration;

  @Value("${jwt.expiration.refresh}")
  private Long refreshTokenExpiration;

  @Value("${jwt.token.prefix:Bearer }")
  private String tokenPrefix;

  @Value("${jwt.header.name:Authorization}")
  private String headerName;

  @Bean
  public JwtUtil jwtUtil() {
    validateConfig();

    JwtUtil jwtUtil = new JwtUtil();
    jwtUtil.setSecret(secret);
    jwtUtil.setAccessTokenExpiration(accessTokenExpiration);
    jwtUtil.setRefreshTokenExpiration(refreshTokenExpiration);

    return jwtUtil;
  }

  private void validateConfig() {
    if (!StringUtils.hasText(secret)) {
      throw new IllegalStateException("JWT secret 不能為空");
    }
    if (accessTokenExpiration == null || accessTokenExpiration <= 0) {
      throw new IllegalStateException("存取令牌時效設定無效");
    }
    if (refreshTokenExpiration == null || refreshTokenExpiration <= 0) {
      throw new IllegalStateException("重整令牌時效設定無效");
    }
    if (refreshTokenExpiration <= accessTokenExpiration) {
      throw new IllegalStateException("重整令牌時效必須大於存取令牌時效");
    }
    if (!StringUtils.hasText(tokenPrefix)) {
      throw new IllegalStateException("Token 前綴不能為空");
    }
    if (!StringUtils.hasText(headerName)) {
      throw new IllegalStateException("Header 名稱不能為空");
    }
  }
}
