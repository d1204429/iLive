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
      if (request.getMethod().equals("OPTIONS")) {
        response.setStatus(HttpServletResponse.SC_OK);
        configureCorsHeaders(response, request.getHeader("Origin"));
        return;
      }

      String authHeader = request.getHeader("Authorization");
      logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
      logger.debug("Auth header: {}", authHeader);

      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = jwtUtil.getTokenFromHeader(authHeader);

        if (token != null) {
          try {
            int userId = jwtUtil.getUserIdFromToken(token);

            if (userId != 0) {
              if (jwtUtil.isTokenExpired(token)) {
                handleTokenExpired(response);
                return;
              }

              UsernamePasswordAuthenticationToken authentication =
                      new UsernamePasswordAuthenticationToken(
                              String.valueOf(userId),
                              null,
                              new ArrayList<>()
                      );

              SecurityContextHolder.getContext().setAuthentication(authentication);
              logger.debug("User authenticated: {}", userId);
            }
          } catch (Exception e) {
            handleTokenValidationError(response, e);
            return;
          }
        }
      }

      configureCorsHeaders(response, request.getHeader("Origin"));
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      handleFilterError(response, e);
    } finally {
      if (request.getRequestURI().contains("/logout")) {
        SecurityContextHolder.clearContext();
      }
    }
  }

  private void configureCorsHeaders(HttpServletResponse response, String origin) {
    response.setHeader("Access-Control-Allow-Origin", origin);
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
    response.setHeader("Access-Control-Max-Age", "3600");
    response.setHeader("Access-Control-Allow-Headers",
            "Authorization, Content-Type, Accept, Origin, X-Requested-With, Cache-Control");
    response.setHeader("Access-Control-Expose-Headers",
            "Authorization, Access-Control-Allow-Origin, Access-Control-Allow-Credentials");
  }

  private void handleTokenExpired(HttpServletResponse response) throws IOException {
    logger.warn("Token has expired");
    SecurityContextHolder.clearContext();
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"message\":\"Token已過期，請重新登入\"}");
  }

  private void handleTokenValidationError(HttpServletResponse response, Exception e) throws IOException {
    logger.error("Token validation failed: {}", e.getMessage());
    SecurityContextHolder.clearContext();
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"message\":\"無效的Token\"}");
  }

  private void handleFilterError(HttpServletResponse response, Exception e) throws IOException {
    logger.error("Filter error: {}", e.getMessage());
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"message\":\"系統錯誤\"}");
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/v1/users/login") ||
            path.startsWith("/api/v1/users/register") ||
            path.startsWith("/api/v1/users/refresh-token") ||
            path.startsWith("/api/v1/users/logout") ||
            (path.startsWith("/api/v1/products") && request.getMethod().equals("GET")) ||
            path.startsWith("/api/v1/categories") ||
            path.startsWith("/api/v1/promotions") ||
            path.equals("/api/v1/system/health") ||
            path.startsWith("/static/") ||
            path.startsWith("/images/");
  }
}
