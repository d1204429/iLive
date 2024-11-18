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

  /**
   * 用戶註冊
   */
  public User register(User user) {
    if (userRepository.findByUsername(user.getUsername()) != null) {
      throw new RuntimeException("用戶名已存在");
    }

    if (userRepository.findByEmail(user.getEmail()) != null) {
      throw new RuntimeException("電子郵件已存在");
    }

    // 加密密碼
    String hashedPassword = passwordEncoder.encode(user.getPasswordHash());
    user.setPasswordHash(hashedPassword);

    return userRepository.save(user);
  }

  /**
   * 用戶登入
   */
  public Map<String, String> login(String username, String password) {
    User user = userRepository.findByUsername(username);

    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new RuntimeException("用戶名或密碼錯誤");
    }

    String accessToken = jwtUtil.generateAccessToken(username);
    String refreshToken = jwtUtil.generateRefreshToken(username);

    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", accessToken);
    tokens.put("refreshToken", refreshToken);

    return tokens;
  }

  /**
   * 更新用戶資料
   * @param userId 用戶ID
   * @param updatedUser 更新的資料
   * @return 更新後的用戶資料
   */
  public User updateUser(int userId, User updatedUser) {
    User user = userRepository.findById(userId);

    if (user == null) {
      throw new RuntimeException("找不到用戶");
    }

    user.setEmail(updatedUser.getEmail());
    user.setFullName(updatedUser.getFullName());
    user.setPhoneNumber(updatedUser.getPhoneNumber());
    user.setAddress(updatedUser.getAddress());

    return userRepository.update(user);
  }

  /**
   * 獲取用戶資料
   * @param userId 用戶ID
   * @return 用戶資料
   */
  public User getUserById(int userId) {
    User user = userRepository.findById(userId);
    if (user == null) {
      throw new RuntimeException("找不到用戶");
    }
    return user;
  }

  /**
   * 修改密碼
   * @param userId 用戶ID
   * @param oldPassword 舊密碼
   * @param newPassword 新密碼
   */
  public void changePassword(int userId, String oldPassword, String newPassword) {
    User user = getUserById(userId);

    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new RuntimeException("舊密碼錯誤");
    }

    // 將新密碼加密後更新
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.update(user);
  }
}