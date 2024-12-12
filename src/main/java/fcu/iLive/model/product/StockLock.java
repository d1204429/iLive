package fcu.iLive.model.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

/**
 * 庫存鎖定實體類
 * 用於記錄商品庫存的鎖定狀態和相關信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockLock {
  private int lockId;          // 鎖定記錄ID
  private int productId;       // 商品ID
  private int userId;          // 用戶ID
  private int orderId;         // 訂單ID
  private int lockedQuantity;  // 鎖定數量
  private Timestamp expirationTime;  // 過期時間
  private boolean isValid;     // 是否有效
  private int statusId;        // 狀態ID
  private Timestamp createdAt; // 創建時間
  private Timestamp updatedAt; // 更新時間
}
