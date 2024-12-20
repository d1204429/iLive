package fcu.iLive.controller.user;

import fcu.iLive.model.user.User;
import fcu.iLive.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "${spring.web.cors.allowed-origins}", allowCredentials = "true")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody User user) {
    try {
      logger.info("Registering new user: {}", user.getUsername());
      User registeredUser = userService.register(user);
      logger.info("User registered successfully: {}", registeredUser.getUsername());
      return ResponseEntity.ok()
              .body(Map.of(
                      "message", "註冊成功",
                      "user", registeredUser
              ));
    } catch (Exception e) {
      logger.error("Registration failed for user: {}", user.getUsername(), e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
    try {
      String username = loginRequest.get("username");
      String password = loginRequest.get("password");

      if (username == null || password == null) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "用戶名和密碼不能為空"));
      }

      logger.info("Login attempt for user: {}", username);
      Map<String, Object> response = userService.login(username, password);
      logger.info("User logged in successfully: {}", username);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Login failed for user: {}", loginRequest.get("username"), e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshRequest) {
    try {
      String refreshToken = refreshRequest.get("refreshToken");
      if (refreshToken == null) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "刷新令牌不能為空"));
      }

      logger.info("Token refresh attempt");
      Map<String, String> tokens = userService.refreshToken(refreshToken);
      logger.info("Token refreshed successfully");
      return ResponseEntity.ok(tokens);
    } catch (Exception e) {
      logger.error("Token refresh failed", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping("/{userId}")
  public ResponseEntity<?> getUser(@PathVariable int userId) {
    try {
      logger.info("Fetching user info for ID: {}", userId);
      User user = userService.getUserById(userId);
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      logger.error("Failed to fetch user info for ID: {}", userId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(Map.of("message", e.getMessage()));
    }
  }

  @PutMapping("/{userId}")
  public ResponseEntity<?> updateUser(@PathVariable int userId, @RequestBody User user) {
    try {
      logger.info("Updating user info for ID: {}", userId);
      User updatedUser = userService.updateUser(userId, user);
      logger.info("User info updated successfully for ID: {}", userId);
      return ResponseEntity.ok(updatedUser);
    } catch (Exception e) {
      logger.error("Failed to update user info for ID: {}", userId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(Map.of("message", e.getMessage()));
    }
  }

  @PutMapping("/{userId}/password")
  public ResponseEntity<?> changePassword(
          @PathVariable int userId,
          @RequestBody Map<String, String> passwords) {
    try {
      String oldPassword = passwords.get("oldPassword");
      String newPassword = passwords.get("newPassword");

      if (oldPassword == null || newPassword == null) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "舊密碼和新密碼不能為空"));
      }

      logger.info("Changing password for user ID: {}", userId);
      userService.changePassword(userId, oldPassword, newPassword);
      logger.info("Password changed successfully for user ID: {}", userId);
      return ResponseEntity.ok()
              .body(Map.of("message", "密碼修改成功"));
    } catch (Exception e) {
      logger.error("Failed to change password for user ID: {}", userId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
    try {
      logger.info("User logout");
      return ResponseEntity.ok()
              .body(Map.of("message", "登出成功"));
    } catch (Exception e) {
      logger.error("Logout failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("message", e.getMessage()));
    }
  }
}
