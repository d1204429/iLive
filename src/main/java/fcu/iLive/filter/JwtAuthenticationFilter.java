package fcu.iLive.filter;

import fcu.iLive.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil; // JWT工具類

  // 建構子，注入JWT工具類
  public JwtAuthenticationFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = jwtUtil.getTokenFromHeader(authHeader);
      int userId = jwtUtil.getUserIdFromToken(token);

      if (userId != 0 && !jwtUtil.isTokenExpired(token)) {
        // 根據請求路徑判斷是管理員還是一般用戶
        if (request.getRequestURI().startsWith("/api/v1/admin")) {
          request.setAttribute("adminId", userId);
        } else {
          request.setAttribute("userId", userId);
        }

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                String.valueOf(userId),
                null,
                new ArrayList<>()
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    filterChain.doFilter(request, response);
  }
}