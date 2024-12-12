package fcu.iLive.repository.product;

import fcu.iLive.model.product.StockLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.List;

/**
 * 庫存鎖定資料訪問層
 * 處理與資料庫的直接交互操作
 */
@Repository
public class StockLockRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 新增庫存鎖定記錄
   * @param stockLock 庫存鎖定實體
   * @return 新建記錄的ID
   */
  public int insert(StockLock stockLock) {
    String sql = "INSERT INTO StockLocks (ProductId, UserId, OrderId, LockedQuantity, " +
        "ExpirationTime, IsValid, StatusId) VALUES (?, ?, ?, ?, ?, ?, ?)";

    jdbcTemplate.update(sql,
        stockLock.getProductId(),
        stockLock.getUserId(),
        stockLock.getOrderId(),
        stockLock.getLockedQuantity(),
        stockLock.getExpirationTime(),
        stockLock.isValid(),
        stockLock.getStatusId()
    );

    return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", int.class);
  }

  /**
   * 查詢指定商品的有效鎖定記錄
   * @param productId 商品ID
   * @return 鎖定記錄列表
   */
  public List<StockLock> findValidByProductId(int productId) {
    String sql = "SELECT * FROM StockLocks WHERE ProductId = ? AND IsValid = 1";
    return jdbcTemplate.query(sql, this::mapRowToStockLock, productId);
  }

  /**
   * 更新訂單相關的所有鎖定記錄狀態
   * @param orderId 訂單ID
   * @param statusId 新狀態ID
   * @return 更新的記錄數量
   */
  public int updateOrderStatus(int orderId, int statusId) {
    String sql = "UPDATE StockLocks SET StatusId = ?, " +
        "UpdatedAt = CURRENT_TIMESTAMP WHERE OrderId = ?";
    return jdbcTemplate.update(sql, statusId, orderId);
  }

  /**
   * 標記過期的鎖定記錄
   * @param currentTime 當前時間
   * @return 更新的記錄數量
   */
  public int invalidateExpiredLocks(Timestamp currentTime) {
    String sql = "UPDATE StockLocks SET IsValid = 0, StatusId = 4, " +
        "UpdatedAt = CURRENT_TIMESTAMP WHERE ExpirationTime < ? AND IsValid = 1";
    return jdbcTemplate.update(sql, currentTime);
  }

  /**
   * 查詢過期的鎖定記錄
   * @return 過期的鎖定記錄列表（最多100條）
   */
  public List<StockLock> findExpiredLocks() {
    String sql = "SELECT * FROM StockLocks WHERE IsValid = 0 AND StatusId = 4 " +
        "ORDER BY UpdatedAt DESC LIMIT 100";
    return jdbcTemplate.query(sql, this::mapRowToStockLock);
  }

  /**
   * 查詢訂單的所有鎖定記錄
   * @param orderId 訂單ID
   * @return 鎖定記錄列表
   */
  public List<StockLock> findByOrderId(int orderId) {
    String sql = "SELECT * FROM StockLocks WHERE OrderId = ?";
    return jdbcTemplate.query(sql, this::mapRowToStockLock, orderId);
  }

  /**
   * ResultSet映射到StockLock物件
   */
  private StockLock mapRowToStockLock(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
    StockLock stockLock = new StockLock();
    stockLock.setLockId(rs.getInt("LockId"));
    stockLock.setProductId(rs.getInt("ProductId"));
    stockLock.setUserId(rs.getInt("UserId"));
    stockLock.setOrderId(rs.getInt("OrderId"));
    stockLock.setLockedQuantity(rs.getInt("LockedQuantity"));
    stockLock.setExpirationTime(rs.getTimestamp("ExpirationTime"));
    stockLock.setValid(rs.getBoolean("IsValid"));
    stockLock.setStatusId(rs.getInt("StatusId"));
    stockLock.setCreatedAt(rs.getTimestamp("CreatedAt"));
    stockLock.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
    return stockLock;
  }
}
