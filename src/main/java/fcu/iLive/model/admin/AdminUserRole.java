//管理員角色關聯實體

package fcu.iLive.model.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserRole {
  private int adminId;                // AdminID INT
  private int roleId;                 // RoleID INT
  private Admin admin;                // 關聯對象
  private AdminRole role;             // 關聯對象

  // Constructor, Getters and Setters
}