//使用者實體

package fcu.iLive.model.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
  private int userId;                 // 使用者ID
  private String username;            // 使用者名稱
  private String passwordHash;        // 密碼雜湊值
  private String email;               // 電子郵件
  private String fullName;            // 使用者全名
  private String phoneNumber;         // 聯絡電話
  private String address;             // 地址
  private LocalDateTime createdAt;    // 註冊時間
  private LocalDateTime updatedAt;    // 更新時間

  // Constructor, Getters and Setters
}


