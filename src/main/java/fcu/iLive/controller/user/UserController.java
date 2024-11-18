package fcu.iLive.controller.user;

import fcu.iLive.model.user.User;
import fcu.iLive.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  @Autowired
  private UserService userService;

  // 明確的路徑要放在上面
  @RequestMapping(value = "/register", method = RequestMethod.POST)
  public ResponseEntity<?> register(@RequestBody User user) {
    try {
      User registeredUser = userService.register(user);
      return ResponseEntity.ok(registeredUser);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
    try {
      String username = loginRequest.get("username");
      String password = loginRequest.get("password");
      Map<String, String> tokens = userService.login(username, password);
      return ResponseEntity.ok(tokens);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 帶參數的路徑放在下面
  @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
  public ResponseEntity<?> getUser(@PathVariable int userId) {
    try {
      User user = userService.getUserById(userId);
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateUser(@PathVariable int userId, @RequestBody User user) {
    try {
      User updatedUser = userService.updateUser(userId, user);
      return ResponseEntity.ok(updatedUser);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @RequestMapping(value = "/{userId}/password", method = RequestMethod.PUT)
  public ResponseEntity<?> changePassword(
      @PathVariable int userId,
      @RequestBody Map<String, String> passwords) {
    try {
      String oldPassword = passwords.get("oldPassword");
      String newPassword = passwords.get("newPassword");
      userService.changePassword(userId, oldPassword, newPassword);
      return ResponseEntity.ok().body("密碼修改成功");
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}