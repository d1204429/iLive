package fcu.iLive.util;

import fcu.iLive.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
  private String secret;
  private Long accessTokenExpiration;
  private Long refreshTokenExpiration;

  // 預設建構子
  public JwtUtil() {
  }

  // 帶參數的建構子
  public JwtUtil(String secret, Long accessTokenExpiration, Long refreshTokenExpiration) {
    this.secret = secret;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  // 產生 SecretKey
  private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  // 產生存取令牌
  public String generateAccessToken(String username) {
    return generateToken(username, accessTokenExpiration);
  }

  // 產生重整令牌
  public String generateRefreshToken(String username) {
    return generateToken(username, refreshTokenExpiration);
  }

  // 建立 Token
  private String generateToken(String username, Long expiration) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("username", username);

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
        .signWith(getSigningKey(), SignatureAlgorithm.HS512)
        .compact();
  }

  // 使用重整令牌刷新存取令牌
  public String refreshAccessToken(String refreshToken) {
    if (isTokenExpired(refreshToken)) {
      throw new JwtException("Refresh token 已過期");
    }
    String username = getUsernameFromToken(refreshToken);
    return generateAccessToken(username);
  }

  // 從 Token 取得用戶名稱
  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  // 驗證 Token 是否過期
  public Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  // 驗證 Token
  public boolean validateToken(String token, String username) {
    try {
      final String tokenUsername = getUsernameFromToken(token);
      return (tokenUsername.equals(username) && !isTokenExpired(token));
    } catch (JwtException e) {
      return false;
    }
  }

  // 從 Token 取得過期時間
  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  // 從 Token 取得指定的資料
  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  // 從 Token 解析所有資料
  private Claims getAllClaimsFromToken(String token) {
    try {
      return Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw new JwtException("Token 已過期");
    } catch (SignatureException e) {
      throw new JwtException("Token 驗證失敗");
    } catch (Exception e) {
      throw new JwtException("無效的 Token");
    }
  }

  // 從請求標頭取得 Token
  public String getTokenFromHeader(String header) {
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }

  // Setters
  public void setSecret(String secret) {
    this.secret = secret;
  }

  public void setAccessTokenExpiration(Long accessTokenExpiration) {
    this.accessTokenExpiration = accessTokenExpiration;
  }

  public void setRefreshTokenExpiration(Long refreshTokenExpiration) {
    this.refreshTokenExpiration = refreshTokenExpiration;
  }
}