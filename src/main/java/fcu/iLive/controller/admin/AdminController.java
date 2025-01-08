//管理員相關 API (/admin/users/**)

package fcu.iLive.controller.admin;



import fcu.iLive.model.user.User;

import java.util.List;
import java.util.stream.Collectors;
import fcu.iLive.model.admin.Admin;
import fcu.iLive.model.admin.AdminRole;
import fcu.iLive.service.admin.AdminService;
import fcu.iLive.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

  private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

  @Autowired
  private AdminService adminService;
  @Autowired
  private UserService userService;

  /**
   * 註冊新管理員
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody Admin admin) {
    try {
      Admin registeredAdmin = adminService.register(admin);
      return ResponseEntity.ok(registeredAdmin);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(Map.of("message", e.getMessage()));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409)
          .body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "註冊失敗: " + e.getMessage()));
    }
  }

  /**
   * 管理員登入
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
    try {
      String username = loginRequest.get("username");
      String password = loginRequest.get("passwordHash");
      Map<String, String> result = adminService.login(username, password);
      return ResponseEntity.ok(result);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(401)
          .body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "登入失敗: " + e.getMessage()));
    }
  }

  /**
   * 啟用管理員帳號
   */
  @PostMapping("/accounts/{id}/activate")
  public ResponseEntity<?> activateAccount(
      @PathVariable int id) {
        //,@RequestAttribute("adminId") int operatorId
    try {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      int operatorId = Integer.parseInt(auth.getPrincipal().toString());
      adminService.activateAccount(id, operatorId);
      return ResponseEntity.ok()
          .body(Map.of("message", "帳號已啟用"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(Map.of("message", e.getMessage()));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(403)
          .body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "啟用失敗: " + e.getMessage()));
    }
  }

  /**
   * 註銷管理員帳號
   */
  @PostMapping("/accounts/{id}/disable")
  public ResponseEntity<?> disableAccount(
      @PathVariable int id) {
        //,@RequestAttribute("adminId") int operatorId
    try {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      int operatorId = Integer.parseInt(auth.getPrincipal().toString());
      adminService.disableAccount(id, operatorId);
      return ResponseEntity.ok()
          .body(Map.of("message", "帳號已註銷"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(Map.of("message", e.getMessage()));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(403)
          .body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "註銷失敗: " + e.getMessage()));
    }
  }

  /**
   * 分配角色給管理員
   */
  @PostMapping("/accounts/{id}/roles/{roleId}")
  public ResponseEntity<?> assignRole(
      @PathVariable("id") int id,
      @PathVariable("roleId") int roleId){
    try {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      int operatorId = Integer.parseInt(auth.getPrincipal().toString());
      adminService.assignRole(id, roleId, operatorId);
      return ResponseEntity.ok()
          .body(Map.of("message", "角色分配成功"));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(403)
          .body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "角色分配失敗: " + e.getMessage()));
    }
  }

  /**
   * 移除管理員的角色
   */
  @DeleteMapping("/accounts/{id}/roles/{roleId}")
  public ResponseEntity<?> removeRole(
      @PathVariable("id") int id,
      @PathVariable("roleId") int roleId) {
    try {
      // 從 Security Context 取得操作者 ID
      var auth = SecurityContextHolder.getContext().getAuthentication();
      int operatorId = Integer.parseInt(auth.getPrincipal().toString());

      adminService.removeRole(id, roleId, operatorId);
      return ResponseEntity.ok()
          .body(Map.of("message", "角色移除成功"));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(403)
          .body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "角色移除失敗: " + e.getMessage()));
    }
  }

  /**
   * 獲取管理員資料
   */
  @GetMapping("/accounts/{id}")
  public ResponseEntity<?> getAdmin(
      @PathVariable int id,
      @RequestAttribute("adminId") int operatorId) {
    try {
      Admin admin = adminService.getAdmin(id);
      if (admin == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(admin);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "獲取管理員資料失敗: " + e.getMessage()));
    }
  }

  /**
   * 獲取管理員的角色列表
   */
  @GetMapping("/accounts/{id}/roles")
  public ResponseEntity<?> getAdminRoles(@PathVariable int id) {
    try {
      List<AdminRole> roles = adminService.getAdminRoles(id);
      return ResponseEntity.ok(roles);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "獲取角色列表失敗: " + e.getMessage()));
    }
  }

  /**
   * 獲取所有管理員的角色列表
   */
  @GetMapping("/accounts/roles")
  public ResponseEntity<?> getAllAdminRoles() {
    try {
      Map<Integer, List<AdminRole>> allRoles = adminService.getAllAdminRoles();
      return ResponseEntity.ok(allRoles);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "獲取角色列表失敗: " + e.getMessage()));
    }
  }

  /**
   * 獲取所有管理員列表
   */
  @GetMapping("/accounts")
  public ResponseEntity<?> getAllAdmins() {
    //@RequestAttribute("adminId") int operatorId
    try {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      int operatorId = Integer.parseInt(auth.getPrincipal().toString());
      // 檢查是否有查看權限
      if (!adminService.hasPermission(operatorId, AdminService.PERMISSION_MANAGE_USERS)) {
        return ResponseEntity.status(403)
            .body(Map.of("message", "無權限查看管理員列表"));
      }

      List<Admin> admins = adminService.getAdminList();
      return ResponseEntity.ok(admins);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "獲取管理員列表失敗: " + e.getMessage()));
    }
  }

  @GetMapping("/users")
  public ResponseEntity<?> getAllUsers() {
    try {
      logger.info("Fetching all users");
      List<User> users = userService.getAllUsers();
      return ResponseEntity.ok(users);
    } catch (Exception e) {
      logger.error("Failed to fetch users", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("message", e.getMessage()));
    }
  }

  @PutMapping("/users/{userId}")
  public ResponseEntity<?> updateUser(@PathVariable int userId, @RequestBody User user) {
    try {
      logger.info("Admin updating user info for ID: {}", userId);
      User updatedUser = userService.updateUser(userId, user);
      logger.info("User info updated successfully for ID: {}", userId);
      return ResponseEntity.ok(updatedUser);
    } catch (Exception e) {
      logger.error("Failed to update user info for ID: {}", userId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", e.getMessage()));
    }
  }

  @PutMapping("/users/{userId}/reset-password")
  public ResponseEntity<?> resetPassword(
      @PathVariable int userId,
      @RequestBody Map<String, String> passwordRequest) {
    try {
      String newPassword = passwordRequest.get("newPassword");
      if (newPassword == null) {
        return ResponseEntity.badRequest()
            .body(Map.of("message", "新密碼不能為空"));
      }

      logger.info("Admin resetting password for user ID: {}", userId);
      userService.adminResetPassword(userId, newPassword);
      logger.info("Password reset successfully for user ID: {}", userId);
      return ResponseEntity.ok()
          .body(Map.of("message", "密碼重設成功"));
    } catch (Exception e) {
      logger.error("Failed to reset password for user ID: {}", userId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", e.getMessage()));
    }
  }
}