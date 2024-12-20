package fcu.iLive.controller.user;

import fcu.iLive.model.user.User;
import fcu.iLive.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "${spring.web.cors.allowed-origins}", allowCredentials = "true")
public class UserController {

  @Autowired
  private UserService userService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody User user) {
    try {
      User registeredUser = userService.register(user);
      return ResponseEntity.ok(registeredUser);
    } catch (Exception e) {
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

      Map<String, Object> tokens = userService.login(username, password);
      return ResponseEntity.ok(tokens);
    } catch (Exception e) {
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

      Map<String, String> tokens = userService.refreshToken(refreshToken);
      return ResponseEntity.ok(tokens);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping("/{userId}")
  public ResponseEntity<?> getUser(@PathVariable int userId) {
    try {
      User user = userService.getUserById(userId);
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(Map.of("message", e.getMessage()));
    }
  }

  @PutMapping("/{userId}")
  public ResponseEntity<?> updateUser(@PathVariable int userId, @RequestBody User user) {
    try {
      User updatedUser = userService.updateUser(userId, user);
      return ResponseEntity.ok(updatedUser);
    } catch (Exception e) {
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

      userService.changePassword(userId, oldPassword, newPassword);
      return ResponseEntity.ok()
              .body(Map.of("message", "密碼修改成功"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
    try {
      // 可以在這裡添加登出相關邏輯
      return ResponseEntity.ok()
              .body(Map.of("message", "登出成功"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("message", e.getMessage()));
    }
  }
}
