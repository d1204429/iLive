package fcu.iLive.service.order;

import fcu.iLive.exception.BusinessException;
import fcu.iLive.model.cart.CartItems;
import fcu.iLive.model.cart.ShoppingCart;
import fcu.iLive.model.order.Order;
import fcu.iLive.model.order.OrderItem;
import fcu.iLive.model.product.Product;
import fcu.iLive.model.product.StockLock;
import fcu.iLive.repository.cart.CartItemsRepository;
import fcu.iLive.repository.cart.ShoppingCartRepository;
import fcu.iLive.repository.order.OrderRepository;
import fcu.iLive.repository.product.ProductRepository;
import fcu.iLive.repository.product.StockLockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 訂單服務
 * 處理訂單相關的業務邏輯，包括創建訂單、支付處理、訂單管理等
 */
@Service
public class OrderService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private CartItemsRepository cartItemsRepository;

  @Autowired
  private ShoppingCartRepository shoppingCartRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private StockLockRepository stockLockRepository;

  @Autowired
  private PaymentService paymentService;

  /**
   * 從購物車創建訂單
   * @param userId 用戶ID
   * @param shippingAddress 配送地址
   * @return 訂單ID
   */
  @Transactional
  public int createOrderFromCart(int userId, String shippingAddress) {
    // 1. 獲取購物車信息
    ShoppingCart cart = shoppingCartRepository.findByUserId(userId);
    if (cart == null) {
      throw new BusinessException("購物車不存在");
    }

    List<CartItems> cartItems = cartItemsRepository.findByCartId(cart.getCartId());
    if (cartItems.isEmpty()) {
      throw new BusinessException("購物車為空");
    }

    // 2. 計算訂單金額並準備訂單項目
    BigDecimal totalAmount = BigDecimal.ZERO;
    List<OrderItem> orderItems = new ArrayList<>();
    List<StockLock> stockLocks = new ArrayList<>();

    for (CartItems item : cartItems) {
      Product product = productRepository.findById(item.getProductId());
      if (product == null) {
        throw new BusinessException("商品不存在：" + item.getProductId());
      }

      // 檢查庫存
      if (!productRepository.hasEnoughStock(product.getProductId(), item.getQuantity())) {
        throw new BusinessException("商品庫存不足：" + product.getName());
      }

      // 計算金額
      BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
      totalAmount = totalAmount.add(itemTotal);

      // 準備訂單項目
      OrderItem orderItem = new OrderItem();
      orderItem.setProductId(product.getProductId());
      orderItem.setQuantity(item.getQuantity());
      orderItem.setPrice(product.getPrice());
      orderItems.add(orderItem);

      // 準備庫存鎖定
      StockLock stockLock = new StockLock();
      stockLock.setProductId(product.getProductId());
      stockLock.setUserId(userId);
      stockLock.setLockedQuantity(item.getQuantity());
      stockLock.setStatusId(1); // 訂單鎖定狀態
      stockLock.setExpirationTime(Timestamp.valueOf(LocalDateTime.now().plusMinutes(30)));
      stockLock.setValid(true);
      stockLocks.add(stockLock);
    }

    // 3. 創建訂單
    Order order = new Order();
    order.setUserId(userId);
    order.setTotalAmount(totalAmount);
    order.setShippingAddress(shippingAddress);
    order.setStatusId(1); // 初始狀態

    int orderId = orderRepository.create(order);

    // 4. 創建訂單項目
    orderItems.forEach(item -> item.setOrderId(orderId));
    orderRepository.createOrderItems(orderItems);

    // 5. 創建庫存鎖定
    stockLocks.forEach(lock -> {
      lock.setOrderId(orderId);
      int lockId = stockLockRepository.insert(lock);
      productRepository.updateLockedStock(lock.getProductId(),
          productRepository.findById(lock.getProductId()).getLockedStock() + lock.getLockedQuantity());
    });

    // 6. 清空購物車
    cartItemsRepository.deleteAllByCartId(cart.getCartId());

    return orderId;
  }

  /**
   * 處理信用卡付款
   * @param orderId 訂單ID
   * @param userId 用戶ID
   * @param cardNumber 信用卡號
   */
  @Transactional
  public void processCreditCardPayment(int orderId, int userId, String cardNumber) {
    // 1. 驗證訂單
    Order order = validateOrderOwnership(orderId, userId);
    if (order.getStatusId() != 1) {
      throw new BusinessException("訂單狀態不正確，僅允許對未支付訂單進行支付");
    }

    // 2. 驗證信用卡
    if (!paymentService.validateCreditCardPayment(cardNumber)) {
      throw new BusinessException("信用卡號格式不正確，請輸入16位數字");
    }

    // 3. 處理付款
    completePayment(order, "CREDIT_CARD");
  }

  /**
   * 處理Apple Pay付款
   * @param orderId 訂單ID
   * @param userId 用戶ID
   * @param applePayToken Apple Pay Token
   */
  @Transactional
  public void processApplePayPayment(int orderId, int userId, String applePayToken) {
    // 1. 驗證訂單
    Order order = validateOrderOwnership(orderId, userId);
    if (order.getStatusId() != 1) {
      throw new BusinessException("訂單狀態不正確，僅允許對未支付訂單進行支付");
    }

    // 2. 驗證Apple Pay
    if (!paymentService.validateApplePayPayment(applePayToken)) {
      throw new BusinessException("Apple Pay驗證失敗，請確認是否已正確設置");
    }

    // 3. 處理付款
    completePayment(order, "APPLE_PAY");
  }

  /**
   * 取消訂單
   * @param orderId 訂單ID
   * @param userId 用戶ID
   */
  @Transactional
  public void cancelOrder(int orderId, int userId) {
    // 1. 驗證訂單
    Order order = validateOrderOwnership(orderId, userId);
    if (order.getStatusId() >= 3) {
      throw new BusinessException("已付款訂單不可取消");
    }

    // 2. 釋放庫存鎖定
    List<StockLock> stockLocks = stockLockRepository.findByOrderId(orderId);
    for (StockLock lock : stockLocks) {
      if (!productRepository.releaseLockedStock(lock.getProductId(), lock.getLockedQuantity())) {
        throw new BusinessException("庫存釋放失敗");
      }
    }

    // 3. 更新訂單和庫存鎖定狀態
    orderRepository.updateStatus(orderId, 4); // 已取消
    stockLockRepository.updateOrderStatus(orderId, 3); // 取消狀態
  }

  /**
   * 處理過期訂單
   * 系統自動執行，處理超時未付款訂單
   */
  @Transactional
  public void handleExpiredOrders() {
    LocalDateTime now = LocalDateTime.now();
    Timestamp currentTime = Timestamp.valueOf(now);

    // 1. 標記過期的鎖定記錄
    stockLockRepository.invalidateExpiredLocks(currentTime);

    // 2. 獲取並處理過期記錄
    List<StockLock> expiredLocks = stockLockRepository.findExpiredLocks();
    for (StockLock lock : expiredLocks) {
      productRepository.releaseLockedStock(lock.getProductId(), lock.getLockedQuantity());
      if (lock.getOrderId() > 0) {
        orderRepository.updateStatus(lock.getOrderId(), 4); // 更新為已取消
      }
    }
  }

  /**
   * 獲取訂單詳情
   * @param orderId 訂單ID
   * @param userId 用戶ID
   * @return 訂單詳情
   */
  public Order getOrderById(int orderId, int userId) {
    return validateOrderOwnership(orderId, userId);
  }

  /**
   * 獲取用戶的所有訂單
   * @param userId 用戶ID
   * @return 訂單列表
   */
  public List<Order> getUserOrders(int userId) {
    return orderRepository.findByUserId(userId);
  }

  /**
   * 完成付款流程
   * @param order 訂單對象
   * @param paymentMethod 支付方式
   */
  private void completePayment(Order order, String paymentMethod) {
    // 1. 更新付款方式、訂單狀態和付款時間
    orderRepository.updatePaymentMethod(order.getOrderId(), paymentMethod);
    orderRepository.updateStatus(order.getOrderId(), 3); // 已付款狀態
    orderRepository.updateOrderDate(order.getOrderId()); // 更新付款時間

    // 2. 處理庫存
    List<StockLock> stockLocks = stockLockRepository.findByOrderId(order.getOrderId());
    for (StockLock lock : stockLocks) {
      if (!productRepository.deductStock(lock.getProductId(), lock.getLockedQuantity())) {
        throw new BusinessException("庫存扣除失敗：商品ID " + lock.getProductId());
      }
    }

    // 3. 更新庫存鎖定狀態
    stockLockRepository.updateOrderStatus(order.getOrderId(), 2); // 已付款狀態
  }

  /**
   * 驗證訂單所有權
   * @param orderId 訂單ID
   * @param userId 用戶ID
   * @return 訂單對象
   */
  private Order validateOrderOwnership(int orderId, int userId) {
    Order order = orderRepository.findById(orderId);
    if (order == null || order.getUserId() != userId) {
      throw new BusinessException("訂單不存在或無權訪問");
    }
    return order;
  }
}