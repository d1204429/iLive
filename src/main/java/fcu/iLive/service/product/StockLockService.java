package fcu.iLive.service.product;

import fcu.iLive.model.product.StockLock;
import fcu.iLive.repository.product.StockLockRepository;
import fcu.iLive.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.util.List;

/**
 * 庫存鎖定服務層
 * 處理庫存鎖定相關的業務邏輯
 */
@Service
public class StockLockService {

  @Autowired
  private StockLockRepository stockLockRepository;

  @Autowired
  private ProductRepository productRepository;

  /**
   * 內部方法：創建訂單時的庫存鎖定
   * @param orderId 訂單ID
   * @param productId 商品ID
   * @param quantity 鎖定數量
   * @param userId 用戶ID
   */
  @Transactional
  public void handleOrderCreated(int orderId, int productId, int quantity, int userId) {
    // 檢查庫存
    if (!productRepository.hasEnoughStock(productId, quantity)) {
      throw new RuntimeException("商品庫存不足");
    }

    // 創建鎖定記錄
    StockLock stockLock = new StockLock();
    stockLock.setProductId(productId);
    stockLock.setUserId(userId);
    stockLock.setOrderId(orderId);
    stockLock.setLockedQuantity(quantity);
    stockLock.setExpirationTime(calculateExpirationTime());
    stockLock.setValid(true);
    stockLock.setStatusId(1); // 下單鎖定狀態

    stockLockRepository.insert(stockLock);

    // 更新商品鎖定庫存
    productRepository.updateLockedStock(productId,
        productRepository.findById(productId).getLockedStock() + quantity);
  }

  /**
   * 內部方法：訂單支付完成時的庫存扣減
   * @param orderId 訂單ID
   */
  @Transactional
  public void handleOrderPaid(int orderId) {
    List<StockLock> locks = stockLockRepository.findByOrderId(orderId);

    for (StockLock lock : locks) {
      // 更新實際庫存和鎖定庫存
      int quantity = lock.getLockedQuantity();
      if (!productRepository.deductStock(lock.getProductId(), quantity)) {
        throw new RuntimeException("庫存扣減失敗");
      }
    }

    // 更新鎖定狀態為已支付
    stockLockRepository.updateOrderStatus(orderId, 2);
  }

  /**
   * 內部方法：訂單取消時的庫存釋放
   * @param orderId 訂單ID
   */
  @Transactional
  public void handleOrderCancelled(int orderId) {
    List<StockLock> locks = stockLockRepository.findByOrderId(orderId);

    for (StockLock lock : locks) {
      // 釋放保留庫存
      int quantity = lock.getLockedQuantity();
      productRepository.releaseLockedStock(lock.getProductId(), quantity);
    }

    // 更新鎖定狀態為已取消
    stockLockRepository.updateOrderStatus(orderId, 3);
  }

  /**
   * 系統排程：處理過期的庫存鎖定
   * 每5分鐘執行一次
   */
  @Scheduled(fixedRate = 300000)
  @Transactional
  public void processExpiredLocks() {
    Timestamp currentTime = new Timestamp(System.currentTimeMillis());
    stockLockRepository.invalidateExpiredLocks(currentTime);
  }

  /**
   * 查詢商品的有效鎖定記錄（供管理員使用）
   * @param productId 商品ID
   * @return 有效的鎖定記錄列表
   */
  public List<StockLock> getValidLocks(int productId) {
    return stockLockRepository.findValidByProductId(productId);
  }

  /**
   * 查詢過期的鎖定記錄（供管理員使用）
   * @return 過期的鎖定記錄列表
   */
  public List<StockLock> getExpiredLocks() {
    return stockLockRepository.findExpiredLocks();
  }

  /**
   * 計算鎖定過期時間（預設30分鐘）
   * @return 過期時間戳
   */
  private Timestamp calculateExpirationTime() {
    return new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000);
  }
}