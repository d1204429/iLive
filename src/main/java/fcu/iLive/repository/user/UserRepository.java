package fcu.iLive.repository.user;

import fcu.iLive.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 將資料庫結果映射到User物件
   */
  private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
    User user = new User();
    user.setUserId(rs.getInt("UserID"));
    user.setUsername(rs.getString("Username"));
    user.setPasswordHash(rs.getString("PasswordHash"));
    user.setEmail(rs.getString("Email"));
    user.setFullName(rs.getString("FullName"));
    user.setPhoneNumber(rs.getString("PhoneNumber"));
    user.setAddress(rs.getString("Address"));
    user.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
    user.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
    return user;
  };

  /**
   * 根據ID查找用戶
   */
  public User findById(int userId) {
    String sql = "SELECT * FROM Users WHERE UserID = ?";
    return jdbcTemplate.query(sql, userRowMapper, userId)
        .stream()
        .findFirst()
        .orElse(null);
  }

  /**
   * 根據用戶名查找用戶
   */
  public User findByUsername(String username) {
    String sql = "SELECT * FROM Users WHERE Username = ?";
    return jdbcTemplate.query(sql, userRowMapper, username)
        .stream()
        .findFirst()
        .orElse(null);
  }

  /**
   * 根據Email查找用戶
   */
  public User findByEmail(String email) {
    String sql = "SELECT * FROM Users WHERE Email = ?";
    return jdbcTemplate.query(sql, userRowMapper, email)
        .stream()
        .findFirst()
        .orElse(null);
  }

  /**
   * 保存新用戶
   */
  public User save(User user) {
    String sql = "INSERT INTO Users (Username, PasswordHash, Email, FullName, PhoneNumber, Address) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    jdbcTemplate.update(sql,
        user.getUsername(),
        user.getPasswordHash(),  // 這裡已經是加密後的密碼
        user.getEmail(),
        user.getFullName(),
        user.getPhoneNumber(),
        user.getAddress()
    );

    return findByUsername(user.getUsername());
  }
  /**
   * 更新用戶資料
   */
  public User update(User user) {
    String sql = "UPDATE Users SET Email = ?, FullName = ?, PhoneNumber = ?, " +
        "Address = ?, PasswordHash = ? WHERE UserID = ?";

    jdbcTemplate.update(sql,
        user.getEmail(),
        user.getFullName(),
        user.getPhoneNumber(),
        user.getAddress(),
        user.getPasswordHash(),
        user.getUserId()
    );

    return findById(user.getUserId());
  }
}