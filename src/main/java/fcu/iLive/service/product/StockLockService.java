//package fcu.iLive.service.product;
//
//import fcu.iLive.model.order.OrderStatusConstants;
//import fcu.iLive.model.product.Product;
//import fcu.iLive.model.product.StockLock;
//import fcu.iLive.model.product.StockLockStatus;
//import fcu.iLive.repository.order.OrderRepository;
//import fcu.iLive.repository.product.StockLockRepository;
//import fcu.iLive.repository.product.ProductRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import java.sql.Timestamp;
//import java.util.List;
//
///**
// * 庫存鎖定服務層
// * 處理庫存鎖定相關的業務邏輯，整合訂單狀態和庫存操作
// */
//@Service
//public class StockLockService {
//
//  @Autowired
//  private StockLockRepository stockLockRepository;
//
//  @Autowired
//  private ProductRepository productRepository;
//
//  @Autowired
//  private OrderRepository orderRepository;
//
//  /**
//   * 創建訂單時的庫存鎖定
//   */
//  @Transactional
//  public void handleOrderCreated(int orderId, int productId, int quantity, int userId) {
//    // 檢查可用庫存
//    Product product = productRepository.findById(productId);
//    if (product == null || product.getAvailableStock() < quantity) {
//      throw new RuntimeException(String.format("商品「%s」庫存不足，剩餘%d件",
//          product.getName(), product.getAvailableStock()));
//    }
//
//    // 建立鎖定記錄
//    StockLock stockLock = new StockLock();
//    stockLock.setProductId(productId);
//    stockLock.setUserId(userId);
//    stockLock.setOrderId(orderId);
//    stockLock.setLockedQuantity(quantity);
//    stockLock.setExpirationTime(calculateExpirationTime());
//    stockLock.setValid(true);
//    stockLock.setStatusId(StockLockStatus.ORDERED);
//
//    stockLockRepository.insert(stockLock);
//
//    // 更新商品鎖定庫存(增加鎖定數量)
//    productRepository.updateLockedStock(productId, quantity);  // 正值:增加保留庫存
//  }
//
//  /**
//   * 訂單支付完成時的庫存扣減
//   */
//  @Transactional
//  public void handleOrderPaid(int orderId) {
//    List<StockLock> locks = stockLockRepository.findByOrderId(orderId);
//    for (StockLock lock : locks) {
//      if (!lock.isValid()) {
//        throw new RuntimeException("庫存鎖定已失效");
//      }
//
//      Product product = productRepository.findById(lock.getProductId());
//      if (product == null) {
//        throw new RuntimeException("商品不存在");
//      }
//
//      int quantity = lock.getLockedQuantity();
//
//      // 扣減實際庫存和保留庫存
//      if (!productRepository.deductStock(lock.getProductId(), quantity)) {
//        throw new RuntimeException(String.format("商品「%s」庫存扣減失敗", product.getName()));
//      }
//
//      // 減少保留庫存
//      productRepository.updateLockedStock(lock.getProductId(), -quantity);  // 負值:減少保留庫存
//    }
//
//    stockLockRepository.updateOrderStatus(orderId, StockLockStatus.PAID);
//  }
//
//  /**
//   * 訂單取消時的庫存釋放
//   */
//  @Transactional
//  public void handleOrderCancelled(int orderId) {
//    List<StockLock> locks = stockLockRepository.findByOrderId(orderId);
//    for (StockLock lock : locks) {
//      Product product = productRepository.findById(lock.getProductId());
//      if (product == null) {
//        continue; // 商品已刪除，忽略
//      }
//
//      int quantity = lock.getLockedQuantity();
//      productRepository.updateLockedStock(lock.getProductId(), -quantity);  // 負值:減少保留庫存
//    }
//
//    stockLockRepository.updateOrderStatus(orderId, StockLockStatus.CANCELLED);
//  }
//
//  /**
//   * 排程：處理過期的庫存鎖定
//   * 每5分鐘執行一次
//   */
//  @Scheduled(fixedRate = 300000)
//  @Transactional
//  public void processExpiredLocks() {
//    Timestamp currentTime = new Timestamp(System.currentTimeMillis());
//
//    // 標記過期記錄
//    stockLockRepository.invalidateExpiredLocks(currentTime);
//
//    // 查詢並處理過期記錄
//    List<StockLock> expiredLocks = stockLockRepository.findExpiredLocks();
//    for (StockLock lock : expiredLocks) {
//      try {
//        productRepository.updateLockedStock(lock.getProductId(), -lock.getLockedQuantity());  // 負值:減少保留庫存
//
//        // 更新訂單狀態為過期
//        orderRepository.updateOrderStatus(lock.getOrderId(), OrderStatusConstants.EXPIRED);
//      } catch (Exception e) {
//        // 記錄錯誤但繼續處理其他記錄
//        // TODO: 加入日誌記錄
//      }
//    }
//  }
//
//  /**
//   * 查詢商品的有效鎖定記錄
//   * @param productId 商品ID
//   * @return 有效的鎖定記錄列表
//   */
//  public List<StockLock> getValidLocks(int productId) {
//    return stockLockRepository.findValidByProductId(productId);
//  }
//
//  /**
//   * 查詢過期的鎖定記錄
//   * @return 過期的鎖定記錄列表
//   */
//  public List<StockLock> getExpiredLocks() {
//    return stockLockRepository.findExpiredLocks();
//  }
//
//  /**
//   * 計算鎖定過期時間（預設30分鐘）
//   * @return 過期時間戳
//   */
//  private Timestamp calculateExpirationTime() {
//    return new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000);
//  }
//}