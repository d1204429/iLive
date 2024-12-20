package fcu.iLive.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import fcu.iLive.exception.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

  @Value("${security.jwt.secret}")
  private String secret;

  @Value("${jwt.expiration.access}")
  private Long accessTokenExpiration;

  @Value("${jwt.expiration.refresh}")
  private Long refreshTokenExpiration;

  @Value("${jwt.token.prefix}")
  private String tokenPrefix;

  @Value("${jwt.header.name}")
  private String headerName;

  private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String getTokenFromHeader(String authHeader) {
    if (authHeader != null && authHeader.startsWith(tokenPrefix + " ")) {
      return authHeader.substring(tokenPrefix.length() + 1);
    }
    return null;
  }

  public String generateAccessToken(int userId) {
    return generateToken(userId, accessTokenExpiration);
  }

  public String generateRefreshToken(int userId) {
    return generateToken(userId, refreshTokenExpiration);
  }

  private String generateToken(int userId, Long expiration) {
    if (expiration == null) {
      logger.error("Token expiration is null");
      throw new JwtException("Token expiration not configured");
    }

    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration * 1000);

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(String.valueOf(userId))
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
  }

  public String refreshAccessToken(String refreshToken) {
    if (isTokenExpired(refreshToken)) {
      logger.warn("Refresh token has expired");
      throw new JwtException("刷新令牌已過期");
    }
    int userId = getUserIdFromToken(refreshToken);
    return generateAccessToken(userId);
  }

  public int getUserIdFromToken(String token) {
    Claims claims = getAllClaimsFromToken(token);
    return claims.get("userId", Integer.class);
  }

  public Boolean isTokenExpired(String token) {
    try {
      final Date expiration = getExpirationDateFromToken(token);
      return expiration.before(new Date());
    } catch (Exception e) {
      logger.error("Error checking token expiration", e);
      return true;
    }
  }

  public boolean validateToken(String token, int userId) {
    try {
      final int tokenUserId = getUserIdFromToken(token);
      return tokenUserId == userId && !isTokenExpired(token);
    } catch (Exception e) {
      logger.error("Token validation failed", e);
      return false;
    }
  }

  public boolean validateRefreshToken(String refreshToken) {
    try {
      if (refreshToken == null) {
        return false;
      }
      Claims claims = getAllClaimsFromToken(refreshToken);
      return !isTokenExpired(refreshToken);
    } catch (Exception e) {
      logger.error("Refresh token validation failed", e);
      return false;
    }
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(String token) {
    try {
      return Jwts.parser()
              .verifyWith(getSigningKey())
              .build()
              .parseSignedClaims(token)
              .getPayload();
    } catch (ExpiredJwtException e) {
      logger.error("Token has expired", e);
      throw new JwtException("令牌已過期");
    } catch (SignatureException e) {
      logger.error("Invalid token signature", e);
      throw new JwtException("無效的令牌簽名");
    } catch (Exception e) {
      logger.error("Token validation failed", e);
      throw new JwtException("令牌驗證失敗");
    }
  }

  public void setSecret(String secret) {
    if (secret == null || secret.trim().isEmpty()) {
      throw new IllegalArgumentException("Secret key cannot be null or empty");
    }
    this.secret = secret;
    logger.debug("JWT secret key has been updated");
  }

  public void setAccessTokenExpiration(Long accessTokenExpiration) {
    if (accessTokenExpiration == null || accessTokenExpiration <= 0) {
      throw new IllegalArgumentException("Access token expiration must be a positive value");
    }
    this.accessTokenExpiration = accessTokenExpiration;
    logger.debug("Access token expiration has been set to: {} seconds", accessTokenExpiration);
  }

  public void setRefreshTokenExpiration(Long refreshTokenExpiration) {
    if (refreshTokenExpiration == null || refreshTokenExpiration <= 0) {
      throw new IllegalArgumentException("Refresh token expiration must be a positive value");
    }
    if (accessTokenExpiration != null && refreshTokenExpiration <= accessTokenExpiration) {
      throw new IllegalArgumentException("Refresh token expiration must be greater than access token expiration");
    }
    this.refreshTokenExpiration = refreshTokenExpiration;
    logger.debug("Refresh token expiration has been set to: {} seconds", refreshTokenExpiration);
  }

}
