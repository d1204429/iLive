package fcu.iLive.service.admin;

import fcu.iLive.model.admin.Admin;
import fcu.iLive.model.admin.AdminRole;
import fcu.iLive.repository.admin.AdminRepository;
import fcu.iLive.util.XssUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fcu.iLive.util.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

  @Autowired
  private AdminRepository adminRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  // 權限常數
  public static final int PERMISSION_MANAGE_USERS = 1;
  public static final int PERMISSION_MANAGE_PRODUCTS = 2;
  public static final int PERMISSION_MANAGE_ORDERS = 4;
  public static final int PERMISSION_MANAGE_PROMOTIONS = 8;
  public static final int PERMISSION_ACTIVATE_ACCOUNTS = 16;
  public static final int PERMISSION_VIEW_DASHBOARD = 32;
  public static final int PERMISSION_MANAGE_DELIVERY = 64;

  /**
   * 管理員註冊
   */
  @Transactional
  public Admin register(Admin admin) {
    // 1. 檢查必要欄位
    if (admin == null || admin.getUsername() == null || admin.getPasswordHash() == null) {
      throw new IllegalArgumentException("帳號與密碼不能為空");
    }

    // 2. 檢查帳號是否已存在
    if (adminRepository.findByUsername(admin.getUsername()) != null) {
      throw new IllegalStateException("帳號已存在");
    }

    // 3. 驗證帳號格式
    if (!admin.getUsername().matches("^i\\d{7}$")) {
      throw new IllegalArgumentException("帳號格式必須為 i 加上 7 位數字");
    }

    // 4. 驗證密碼格式
    validatePassword(admin.getPasswordHash());

    // 5. 驗證Email格式
    validateEmail(admin.getEmail());

    // 6. XSS 防護
    admin.setUsername(XssUtils.sanitize(admin.getUsername()));
    admin.setEmail(XssUtils.sanitize(admin.getEmail()));

    // 7. 密碼加密
    String hashedPassword = passwordEncoder.encode(admin.getPasswordHash());
    admin.setPasswordHash(hashedPassword);

    // 8. 設置初始狀態為未啟用
    admin.setStatus(Admin.STATUS_PENDING);

    return adminRepository.save(admin);
  }

  /**
   * 管理員登入
   */
  public Map<String, String> login(String username, String password) {
    // 1. 查找管理員
    Admin admin = adminRepository.findByUsername(username);

    // 2. 驗證帳號和密碼
    if (admin == null || !passwordEncoder.matches(password, admin.getPasswordHash())) {
      throw new IllegalStateException("帳號或密碼錯誤");
    }

    // 3. 檢查帳號狀態
    if (admin.getStatus() != Admin.STATUS_ACTIVE) {
      throw new IllegalStateException("帳號未啟用或已被停用");
    }

    // 4. 生成 JWT Token
    String accessToken = jwtUtil.generateAccessToken(admin.getAdminId());
    String refreshToken = jwtUtil.generateRefreshToken(admin.getAdminId());

    // 5. 返回令牌和權限
    Map<String, String> result = new HashMap<>();
    result.put("accessToken", accessToken);
    result.put("refreshToken", refreshToken);
    result.put("permissions", String.valueOf(
        adminRepository.getAdminPermissions(admin.getAdminId())
    ));

    return result;
  }

  /**
   * 啟用管理員帳號
   */
  @Transactional
  public void activateAccount(int adminId, int operatorId) {
    // 1. 檢查操作者權限
    if (!hasPermission(operatorId, PERMISSION_ACTIVATE_ACCOUNTS)) {
      throw new IllegalStateException("無啟用帳號權限");
    }

    // 2. 檢查目標帳號是否存在
    Admin admin = findAdminById(adminId);
    if (admin == null) {
      throw new IllegalArgumentException("管理員不存在");
    }

    // 3. 更新狀態
    adminRepository.updateStatus(adminId, Admin.STATUS_ACTIVE);
  }

  /**
   * 註銷管理員帳號
   */
  @Transactional
  public void disableAccount(int adminId, int operatorId) {
    // 改為檢查 ACTIVATE_ACCOUNTS 權限
    if (!hasPermission(operatorId, PERMISSION_ACTIVATE_ACCOUNTS)) {
      throw new IllegalStateException("無註銷帳號權限");
    }

    Admin admin = findAdminById(adminId);
    if (isSuperAdmin(adminId)) {
      throw new IllegalStateException("不能註銷超級管理員帳號");
    }

    adminRepository.updateStatus(adminId, Admin.STATUS_DISABLED);
  }

  /**
   * 分配角色
   */
  @Transactional
  public void assignRole(int adminId, int roleId, int operatorId) {
    // 1. 檢查操作者權限
    boolean isSuperOperator = isSuperAdmin(operatorId);
    boolean hasAssignPermission = hasPermission(operatorId, PERMISSION_MANAGE_USERS);

    if (!isSuperOperator && !hasAssignPermission) {
      throw new IllegalStateException("無權限分配角色");
    }

    // 2. 檢查分配限制
    if (!isSuperOperator && hasAssignPermission) {
      // 一般管理員不能分配超級管理員角色
      if (roleId == 1) {
        throw new IllegalStateException("無權限分配超級管理員角色");
      }
      // 一般管理員不能修改超級管理員的角色
      if (isSuperAdmin(adminId)) {
        throw new IllegalStateException("無權限修改超級管理員的角色");
      }
    }

    // 3. 分配角色
    adminRepository.assignRole(adminId, roleId);
  }

  /**
   * 移除角色
   */
  @Transactional
  public void removeRole(int adminId, int roleId, int operatorId) {
    // 1. 檢查操作者權限
    boolean isSuperOperator = isSuperAdmin(operatorId);
    if (!isSuperOperator && !hasPermission(operatorId, PERMISSION_MANAGE_USERS)) {
      throw new IllegalStateException("無權限移除角色");
    }

    // 2. 禁止移除超級管理員的角色
    if (isSuperAdmin(adminId)) {
      throw new IllegalStateException("無權限移除超級管理員的角色");
    }

    // 3. 移除角色
    adminRepository.removeRole(adminId, roleId);
  }

  /**
   * 獲取管理員資料
   */
  public Admin getAdmin(int adminId) {
    Admin admin = adminRepository.findById(adminId);
    if (admin != null) {
      admin.setPasswordHash(null); // 清除敏感資訊
    }
    return admin;
  }

  /**
   * 獲取管理員角色列表
   */
  public List<AdminRole> getAdminRoles(int adminId) {
    return adminRepository.getAdminRoles(adminId);
  }

  /**
   * 檢查是否為超級管理員
   */
  private boolean isSuperAdmin(int adminId) {
    return adminRepository.getAdminRoles(adminId)
        .stream()
        .anyMatch(role -> role.getRoleId() == 1);  // 1 = SUPER_ADMIN
  }

  /**
   * 檢查是否有特定權限
   */
  public boolean hasPermission(int adminId, int permission) {
    int permissions = adminRepository.getAdminPermissions(adminId);
    return (permissions & permission) == permission;
  }

  /**
   * 驗證密碼格式
   */
  private void validatePassword(String password) {
    if (password.length() < 8 || password.length() > 12) {
      throw new IllegalArgumentException("密碼長度必須為8~12碼");
    }
    if (password.contains(" ")) {
      throw new IllegalArgumentException("密碼不能包含空格");
    }
    if (!password.matches(".*[A-Z].*")) {
      throw new IllegalArgumentException("密碼必須包含至少一個大寫字母");
    }
    if (!password.matches(".*[a-z].*")) {
      throw new IllegalArgumentException("密碼必須包含至少一個小寫字母");
    }
    if (!password.matches(".*[!@#$%&].*")) {
      throw new IllegalArgumentException("密碼必須包含至少一個特殊符號(!@#$%&)");
    }
    if (!password.matches("^[a-zA-Z0-9!@#$%&]+$")) {
      throw new IllegalArgumentException("密碼只能包含英文字母、數字和特殊符號(!@#$%&)");
    }
  }

  /**
   * 驗證Email格式
   */
  private void validateEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email不能為空");
    }

    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    if (!email.matches(emailRegex)) {
      throw new IllegalArgumentException("Email格式不正確");
    }
  }

  /**
   * 根據ID查找管理員
   */
  private Admin findAdminById(int adminId) {
    Admin admin = adminRepository.findById(adminId);
    if (admin == null) {
      throw new IllegalArgumentException("找不到管理員");
    }
    return admin;
  }

  /**
   * 獲取所有管理員列表
   */
  public List<Admin> getAdminList() {
    List<Admin> admins = adminRepository.findAll();
    // 清除敏感資訊
    admins.forEach(admin -> admin.setPasswordHash(null));
    return admins;
  }
}