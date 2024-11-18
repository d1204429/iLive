//管理員權限實體

package fcu.iLive.model.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminPermission {
  private int permissionId;           // PermissionID INT
  private String permissionName;      // PermissionName VARCHAR(100)

  // Constructor, Getters and Setters
}