//管理員實體

package fcu.iLive.model.admin;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
  private int adminId;                // AdminID INT
  private String username;            // Username VARCHAR(50)
  private String passwordHash;        // PasswordHash VARCHAR(255)
  private String email;               // Email VARCHAR(100)
  private LocalDateTime createdAt;    // CreatedAt DATETIME
  private LocalDateTime updatedAt;    // UpdatedAt DATETIME

  // Constructor, Getters and Setters
}