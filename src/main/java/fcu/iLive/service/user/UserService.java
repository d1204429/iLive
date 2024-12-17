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
    // 檢查用戶名是否已存在
    if (userRepository.findByUsername(user.getUsername()) != null) {
      throw new RuntimeException("用戶名已存在");
    }

    // 檢查電子郵件是否已存在
    if (userRepository.findByEmail(user.getEmail()) != null) {
      throw new RuntimeException("電子郵件已存在");
    }

    // 加密密碼
    String hashedPassword = passwordEncoder.encode(user.getPassword());
    user.setPasswordHash(hashedPassword);

    return userRepository.save(user);
  }

  // 用戶登入
  public Map<String, String> login(String username, String password) {
    // 查找用戶
    User user = userRepository.findByUsername(username);

    // 驗證用戶名和密碼
    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new RuntimeException("用戶名或密碼錯誤");
    }

    // 使用用戶ID生成令牌
    String accessToken = jwtUtil.generateAccessToken(user.getUserId());  // 修改這裡
    String refreshToken = jwtUtil.generateRefreshToken(user.getUserId()); // 修改這裡

    // 回傳令牌
    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", accessToken);
    tokens.put("refreshToken", refreshToken);

    return tokens;
  }

  // 更新用戶資料
  public User updateUser(int userId, User updatedUser) {
    User user = userRepository.findById(userId);

    if (user == null) {
      throw new RuntimeException("找不到用戶");
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
    return user;
  }

  // 修改密碼
  public void changePassword(int userId, String oldPassword, String newPassword) {
    User user = getUserById(userId);

    // 驗證舊密碼
    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new RuntimeException("舊密碼錯誤");
    }

    // 將新密碼加密後更新
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.update(user);
  }
}