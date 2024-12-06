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

    // 從請求標頭中獲取Authorization
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      // 解析Token
      String token = jwtUtil.getTokenFromHeader(authHeader);

      // 從Token中獲取用戶ID
      int userId = jwtUtil.getUserIdFromToken(token);

      // 驗證Token有效性
      if (userId != 0 && !jwtUtil.isTokenExpired(token)) {
        // 創建認證對象，將用戶ID轉為字串以符合Spring Security的要求
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                String.valueOf(userId),  // 將userId轉為字串
                null,
                new ArrayList<>()
            );

        // 設置安全上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    // 繼續過濾鏈的處理
    filterChain.doFilter(request, response);
  }
}