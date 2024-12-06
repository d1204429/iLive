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
  // JWT 加密用的密鑰
  private String secret;
  // 存取令牌的有效期限（秒）
  private Long accessTokenExpiration;
  // 重整令牌的有效期限（秒）
  private Long refreshTokenExpiration;

  // 預設建構子
  public JwtUtil() {
  }

  // 帶參數的建構子，初始化密鑰和有效期限
  public JwtUtil(String secret, Long accessTokenExpiration, Long refreshTokenExpiration) {
    this.secret = secret;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  // 產生用於簽署 JWT 的加密金鑰
  private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  // 產生存取令牌
  public String generateAccessToken(int userId) {
    return generateToken(userId, accessTokenExpiration);
  }

  // 產生重整令牌
  public String generateRefreshToken(int userId) {
    return generateToken(userId, refreshTokenExpiration);
  }

  // 建立 Token，直接使用整數型別的 userId
  private String generateToken(int userId, Long expiration) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId); // 直接存入整數型別的 userId

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(String.valueOf(userId)) // 為了相容性，subject 仍使用字串
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
        .signWith(getSigningKey(), SignatureAlgorithm.HS512)
        .compact();
  }

  // 使用重整令牌刷新存取令牌
  public String refreshAccessToken(String refreshToken) {
    if (isTokenExpired(refreshToken)) {
      throw new JwtException("重整令牌已過期");
    }
    int userId = getUserIdFromToken(refreshToken);
    return generateAccessToken(userId);
  }

  // 從 Token 中取得使用者 ID（直接返回整數型別）
  public int getUserIdFromToken(String token) {
    Claims claims = getAllClaimsFromToken(token);
    return claims.get("userId", Integer.class);
  }

  // 檢查令牌是否已過期
  public Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  // 驗證令牌的有效性
  public boolean validateToken(String token, int userId) {
    try {
      final int tokenUserId = getUserIdFromToken(token);
      return (tokenUserId == userId && !isTokenExpired(token));
    } catch (JwtException e) {
      return false;
    }
  }

  // 從令牌中取得過期時間
  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  // 從令牌中取得指定的資料
  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  // 從令牌中解析出所有資料
  private Claims getAllClaimsFromToken(String token) {
    try {
      return Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw new JwtException("令牌已過期");
    } catch (SignatureException e) {
      throw new JwtException("令牌驗證失敗");
    } catch (Exception e) {
      throw new JwtException("無效的令牌");
    }
  }

  // 從請求標頭取得 Token
  public String getTokenFromHeader(String header) {
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7); // 移除 "Bearer " 前綴
    }
    return null;
  }

  // 設定相關屬性的方法
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