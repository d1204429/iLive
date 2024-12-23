package fcu.iLive.repository.admin;

import fcu.iLive.model.admin.Admin;
import fcu.iLive.model.admin.AdminRole;
import fcu.iLive.util.XssUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class AdminRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 根據ID查找管理員
   * @param adminId 管理員ID
   * @return 若不存在返回null
   */
  public Admin findById(int adminId) {
    String sql = "SELECT * FROM Admins WHERE AdminID = ?";
    try {
      return jdbcTemplate.queryForObject(sql, new AdminRowMapper(), adminId);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  /**
   * 根據帳號查找管理員
   * @param username 帳號名稱 (格式為 i + 7碼數字)
   * @return 若不存在返回null
   */
  public Admin findByUsername(String username) {
    String sql = "SELECT * FROM Admins WHERE Username = ?";
    try {
      return jdbcTemplate.queryForObject(sql, new AdminRowMapper(), username);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  /**
   * 將管理員資料存入資料庫
   * @param admin 要存入的管理員資料
   * @return 包含自增ID的管理員資料
   */
  public Admin save(Admin admin) {
    String sql = "INSERT INTO Admins (Username, PasswordHash, Email, Status) VALUES (?, ?, ?, ?)";

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql,
          Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, admin.getUsername());
      ps.setString(2, admin.getPasswordHash());
      ps.setString(3, admin.getEmail());
      ps.setInt(4, admin.getStatus());
      return ps;
    }, keyHolder);

    admin.setAdminId(keyHolder.getKey().intValue());
    return admin;
  }

  /**
   * 更新管理員狀態
   * @param adminId 管理員ID
   * @param status 狀態碼 (0=未啟用, 1=啟用, 2=註銷)
   */
  public void updateStatus(int adminId, int status) {
    String sql = "UPDATE Admins SET Status = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE AdminID = ?";
    jdbcTemplate.update(sql, status, adminId);
  }

  /**
   * 分配角色給管理員
   * @param adminId 管理員ID
   * @param roleId 角色ID
   */
  public void assignRole(int adminId, int roleId) {
    String sql = "INSERT INTO AdminUserRoles (AdminID, RoleID) VALUES (?, ?)";
    jdbcTemplate.update(sql, adminId, roleId);
  }

  /**
   * 移除管理員的指定角色
   * @param adminId 管理員ID
   * @param roleId 角色ID
   */
  public void removeRole(int adminId, int roleId) {
    String sql = "DELETE FROM AdminUserRoles WHERE AdminID = ? AND RoleID = ?";
    jdbcTemplate.update(sql, adminId, roleId);
  }

  /**
   * 獲取管理員的所有角色
   * @param adminId 管理員ID
   * @return 角色列表
   */
  public List<AdminRole> getAdminRoles(int adminId) {
    String sql = """
            SELECT r.* 
            FROM AdminRoles r
            JOIN AdminUserRoles ur ON r.RoleID = ur.RoleID
            WHERE ur.AdminID = ?
            """;
    return jdbcTemplate.query(sql, new AdminRoleRowMapper(), adminId);
  }

  /**
   * 獲取管理員的權限位元值
   * @param adminId 管理員ID
   * @return 權限位元值
   */
  public int getAdminPermissions(int adminId) {
    String sql = """
            SELECT BIT_OR(p.PermissionID) as permissions
            FROM AdminUserRoles ur
            JOIN AdminRolePermissions rp ON ur.RoleID = rp.RoleID
            JOIN AdminPermissions p ON rp.PermissionID = p.PermissionID
            WHERE ur.AdminID = ?
            """;
    Integer permissions = jdbcTemplate.queryForObject(sql, Integer.class, adminId);
    return permissions != null ? permissions : 0;
  }

  /**
   * 獲取所有管理員列表
   * @return 管理員列表
   */
  public List<Admin> findAll() {
    String sql = "SELECT * FROM Admins ORDER BY CreatedAt DESC";
    return jdbcTemplate.query(sql, new AdminRowMapper());
  }
}

/**
 * 管理員資料映射器
 */
class AdminRowMapper implements RowMapper<Admin> {
  @Override
  public Admin mapRow(ResultSet rs, int rowNum) throws SQLException {
    Admin admin = new Admin();
    admin.setAdminId(rs.getInt("AdminID"));
    admin.setUsername(XssUtils.sanitize(rs.getString("Username")));
    admin.setPasswordHash(rs.getString("PasswordHash"));
    admin.setEmail(XssUtils.sanitize(rs.getString("Email")));
    admin.setStatus(rs.getInt("Status"));
    admin.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
    admin.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
    return admin;
  }
}

/**
 * 角色資料映射器
 */
class AdminRoleRowMapper implements RowMapper<AdminRole> {
  @Override
  public AdminRole mapRow(ResultSet rs, int rowNum) throws SQLException {
    AdminRole role = new AdminRole();
    role.setRoleId(rs.getInt("RoleID"));
    role.setRoleName(XssUtils.sanitize(rs.getString("RoleName")));
    return role;
  }
}