package fcu.iLive.service.user;

import fcu.iLive.model.user.User;
import fcu.iLive.repository.user.UserRepository;
import fcu.iLive.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  public User register(User user) {
    logger.info("Registering new user: {}", user.getUsername());
    validateUserData(user);

    if (userRepository.findByUsername(user.getUsername()) != null) {
      logger.warn("Username already exists: {}", user.getUsername());
      throw new RuntimeException("用戶名已存在");
    }

    if (userRepository.findByEmail(user.getEmail()) != null) {
      logger.warn("Email already exists: {}", user.getEmail());
      throw new RuntimeException("電子郵件已存在");
    }

    if (userRepository.findByPhoneNumber(user.getPhoneNumber()) != null) {
      logger.warn("Phone number already exists: {}", user.getPhoneNumber());
      throw new RuntimeException("手機號碼已存在");
    }

    String hashedPassword = passwordEncoder.encode(user.getPassword());
    user.setPasswordHash(hashedPassword);

    User registeredUser = userRepository.save(user);
    logger.info("User registered successfully: {}", user.getUsername());
    return registeredUser;
  }

  public Map<String, Object> login(String username, String password) {
    logger.info("Login attempt for user: {}", username);
    User user = userRepository.findByUsername(username);

    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
      logger.warn("Login failed for user: {}", username);
      throw new RuntimeException("用戶名或密碼錯誤");
    }

    String accessToken = jwtUtil.generateAccessToken(user.getUserId());
    String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

    Map<String, Object> response = new HashMap<>();
    response.put("accessToken", accessToken);
    response.put("refreshToken", refreshToken);
    response.put("user", sanitizeUser(user));
    response.put("status", "success");
    response.put("message", "登入成功");

    logger.info("User logged in successfully: {}", username);
    return response;
  }

  public Map<String, String> refreshToken(String refreshToken) {
    logger.info("Attempting to refresh token");
    if (!jwtUtil.validateRefreshToken(refreshToken)) {
      logger.warn("Invalid refresh token");
      throw new RuntimeException("無效的刷新令牌");
    }

    int userId = jwtUtil.getUserIdFromToken(refreshToken);
    User user = getUserById(userId);

    if (user == null) {
      logger.warn("User not found for token refresh: {}", userId);
      throw new RuntimeException("用戶不存在");
    }

    String newAccessToken = jwtUtil.generateAccessToken(userId);
    String newRefreshToken = jwtUtil.generateRefreshToken(userId);

    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", newAccessToken);
    tokens.put("refreshToken", newRefreshToken);
    tokens.put("status", "success");

    logger.info("Token refreshed successfully for user: {}", userId);
    return tokens;
  }

  public User updateUser(int userId, User updatedUser) {
    logger.info("Updating user info for ID: {}", userId);
    User user = userRepository.findById(userId);

    if (user == null) {
      logger.warn("User not found: {}", userId);
      throw new RuntimeException("找不到用戶");
    }

    User existingUserWithEmail = userRepository.findByEmail(updatedUser.getEmail());
    if (existingUserWithEmail != null && existingUserWithEmail.getUserId() != userId) {
      logger.warn("Email already in use: {}", updatedUser.getEmail());
      throw new RuntimeException("此電子郵件已被使用");
    }

    user.setEmail(updatedUser.getEmail());
    user.setFullName(updatedUser.getFullName());
    user.setPhoneNumber(updatedUser.getPhoneNumber());
    user.setAddress(updatedUser.getAddress());

    User updated = userRepository.update(user);
    logger.info("User info updated successfully: {}", userId);
    return updated;
  }

  public User getUserById(int userId) {
    logger.info("Fetching user info for ID: {}", userId);
    User user = userRepository.findById(userId);
    if (user == null) {
      logger.warn("User not found: {}", userId);
      throw new RuntimeException("找不到用戶");
    }
    return sanitizeUser(user);
  }

  public void changePassword(int userId, String oldPassword, String newPassword) {
    logger.info("Changing password for user ID: {}", userId);
    User user = getUserById(userId);

    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      logger.warn("Old password incorrect for user: {}", userId);
      throw new RuntimeException("舊密碼錯誤");
    }

    if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
      logger.warn("New password same as old password for user: {}", userId);
      throw new RuntimeException("新密碼不能與舊密碼相同");
    }

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.update(user);
    logger.info("Password changed successfully for user: {}", userId);
  }

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
    if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
      throw new RuntimeException("手機號碼不能為空");
    }
  }

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
