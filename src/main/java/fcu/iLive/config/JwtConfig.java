package fcu.iLive.config;

import fcu.iLive.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class JwtConfig {

  @Value("${security.jwt.secret}")
  private String secret;

  @Value("${jwt.expiration.access}")     // 存取令牌時效，單位：秒
  private Long accessTokenExpiration;

  @Value("${jwt.expiration.refresh}")    // 重整令牌時效，單位：秒
  private Long refreshTokenExpiration;

  @Bean
  public JwtUtil jwtUtil() {
    // 檢查必要的配置是否存在
    if (!StringUtils.hasText(secret)) {
      throw new IllegalStateException("JWT secret 不能為空");
    }
    if (accessTokenExpiration == null || accessTokenExpiration <= 0) {
      throw new IllegalStateException("存取令牌時效設定無效");
    }
    if (refreshTokenExpiration == null || refreshTokenExpiration <= 0) {
      throw new IllegalStateException("重整令牌時效設定無效");
    }

    return new JwtUtil(secret, accessTokenExpiration, refreshTokenExpiration);
  }
}