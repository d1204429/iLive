//管理員角色實體

package fcu.iLive.model.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRole {
  private int roleId;                 // RoleID INT
  private String roleName;            // RoleName VARCHAR(100)

  // Constructor, Getters and Setters
}