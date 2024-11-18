//角色權限關聯實體

package fcu.iLive.model.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRolePermission {
  private int roleId;                 // RoleID INT
  private int permissionId;           // PermissionID INT
  private AdminRole role;             // 關聯對象
  private AdminPermission permission; // 關聯對象

  // Constructor, Getters and Setters
}
