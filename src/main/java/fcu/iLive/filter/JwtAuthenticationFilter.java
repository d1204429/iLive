package fcu.iLive.filter;

import fcu.iLive.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final JwtUtil jwtUtil;

  public JwtAuthenticationFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    try {
      // 檢查是否為預檢請求
      if (request.getMethod().equals("OPTIONS")) {
        filterChain.doFilter(request, response);
        return;
      }

      String authHeader = request.getHeader("Authorization");
      logger.debug("Auth header: {}", authHeader);

      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = jwtUtil.getTokenFromHeader(authHeader);

        if (token != null) {
          try {
            // 驗證 Token
            int userId = jwtUtil.getUserIdFromToken(token);

            if (userId != 0 && !jwtUtil.isTokenExpired(token)) {
              UsernamePasswordAuthenticationToken authentication =
                      new UsernamePasswordAuthenticationToken(
                              String.valueOf(userId),
                              null,
                              new ArrayList<>()
                      );

              SecurityContextHolder.getContext().setAuthentication(authentication);
              logger.debug("User authenticated: {}", userId);
            } else {
              logger.warn("Invalid token for user: {}", userId);
            }
          } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token validation failed");
            return;
          }
        }
      }

      // 添加 CORS 標頭
      response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
      response.setHeader("Access-Control-Allow-Credentials", "true");
      response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
      response.setHeader("Access-Control-Max-Age", "3600");
      response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, X-Requested-With, remember-me");

      filterChain.doFilter(request, response);
    } catch (Exception e) {
      logger.error("Filter error: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write("Internal server error");
    } finally {
      // 清理上下文
      if (request.getRequestURI().contains("/logout")) {
        SecurityContextHolder.clearContext();
      }
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/v1/users/login") ||
            path.startsWith("/api/v1/users/register") ||
            path.startsWith("/api/v1/products") && request.getMethod().equals("GET");
  }
}
