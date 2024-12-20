package fcu.iLive.service.user;

import fcu.iLive.model.user.User;
import fcu.iLive.repository.user.UserRepository;
import fcu.iLive.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  // 用戶註冊
  public User register(User user) {
    // 檢查必要欄位
    validateUserData(user);

    // 檢查用戶名是否已存在
    User existingUserWithUsername = userRepository.findByUsername(user.getUsername());
    if (existingUserWithUsername != null) {
      throw new RuntimeException("用戶名已存在");
    }

    // 檢查電子郵件是否已存在
    User existingUserWithEmail = userRepository.findByEmail(user.getEmail());
    if (existingUserWithEmail != null) {
      throw new RuntimeException("電子郵件已存在");
    }

    // 檢查手機號碼是否已存在
// 檢查手機號碼是否已存在
    User existingUserWithPhone = userRepository.findByPhoneNumber(user.getPhoneNumber());
    if (existingUserWithPhone != null) {
      throw new RuntimeException("手機號碼已存在");
    }


    // 加密密碼
    String hashedPassword = passwordEncoder.encode(user.getPassword());
    user.setPasswordHash(hashedPassword);

    return userRepository.save(user);
  }


  // 用戶登入
  public Map<String, Object> login(String username, String password) {
    // 查找用戶
    User user = userRepository.findByUsername(username);

    // 驗證用戶名和密碼
    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new RuntimeException("用戶名或密碼錯誤");
    }

    // 生成令牌
    String accessToken = jwtUtil.generateAccessToken(user.getUserId());
    String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

    // 準備回傳數據
    Map<String, Object> response = new HashMap<>();
    response.put("accessToken", accessToken);
    response.put("refreshToken", refreshToken);
    response.put("user", sanitizeUser(user));

    return response;
  }

  // 刷新令牌
  public Map<String, String> refreshToken(String refreshToken) {
    if (!jwtUtil.validateRefreshToken(refreshToken)) {
      throw new RuntimeException("無效的刷新令牌");
    }

    int userId = jwtUtil.getUserIdFromToken(refreshToken);
    User user = getUserById(userId);

    if (user == null) {
      throw new RuntimeException("用戶不存在");
    }

    String newAccessToken = jwtUtil.generateAccessToken(userId);
    String newRefreshToken = jwtUtil.generateRefreshToken(userId);

    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", newAccessToken);
    tokens.put("refreshToken", newRefreshToken);

    return tokens;
  }

  // 更新用戶資料
  public User updateUser(int userId, User updatedUser) {
    User user = userRepository.findById(userId);

    if (user == null) {
      throw new RuntimeException("找不到用戶");
    }

    // 檢查郵箱是否被其他用戶使用
    User existingUserWithEmail = userRepository.findByEmail(updatedUser.getEmail());
    if (existingUserWithEmail != null && existingUserWithEmail.getUserId() != userId) {
      throw new RuntimeException("此電子郵件已被使用");
    }

    // 更新用戶資料
    user.setEmail(updatedUser.getEmail());
    user.setFullName(updatedUser.getFullName());
    user.setPhoneNumber(updatedUser.getPhoneNumber());
    user.setAddress(updatedUser.getAddress());

    return userRepository.update(user);
  }

  // 獲取用戶資料
  public User getUserById(int userId) {
    User user = userRepository.findById(userId);
    if (user == null) {
      throw new RuntimeException("找不到用戶");
    }
    return sanitizeUser(user);
  }

  // 修改密碼
  public void changePassword(int userId, String oldPassword, String newPassword) {
    User user = getUserById(userId);

    // 驗證舊密碼
    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new RuntimeException("舊密碼錯誤");
    }

    // 檢查新密碼是否與舊密碼相同
    if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
      throw new RuntimeException("新密碼不能與舊密碼相同");
    }

    // 將新密碼加密後更新
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.update(user);
  }

  // 驗證用戶數據
  private void validateUserData(User user) {
    if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
      throw new RuntimeException("用戶名不能為空");
    }
    if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
      throw new RuntimeException("密碼不能為空");
    }
    if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
      throw new RuntimeException("電子郵件不能為空");
    }
  }

  // 清理敏感數據
  private User sanitizeUser(User user) {
    User sanitizedUser = new User();
    sanitizedUser.setUserId(user.getUserId());
    sanitizedUser.setUsername(user.getUsername());
    sanitizedUser.setEmail(user.getEmail());
    sanitizedUser.setFullName(user.getFullName());
    sanitizedUser.setPhoneNumber(user.getPhoneNumber());
    sanitizedUser.setAddress(user.getAddress());
    sanitizedUser.setCreatedAt(user.getCreatedAt());
    sanitizedUser.setUpdatedAt(user.getUpdatedAt());
    return sanitizedUser;
  }
}
