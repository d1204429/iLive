package fcu.iLive.service.order;

import fcu.iLive.model.cart.CartItems;
import fcu.iLive.model.order.Order;
import fcu.iLive.model.order.OrderItem;
import fcu.iLive.model.order.OrderStatusConstants;
import fcu.iLive.model.product.Product;
import fcu.iLive.repository.order.OrderRepository;
import fcu.iLive.repository.order.OrderItemRepository;
import fcu.iLive.repository.product.ProductRepository;
import fcu.iLive.service.cart.CartService;
//import fcu.iLive.service.cart.ShoppingCartService;
//import fcu.iLive.service.product.StockLockService;
import fcu.iLive.service.promotion.ProductPromotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 訂單服務層
 * 處理訂單相關的業務邏輯和價格計算
 */
@Slf4j
@Service
public class OrderService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderItemRepository orderItemRepository;

  @Autowired
  private ProductRepository productRepository;

//  @Autowired
//  private StockLockService stockLockService;

  @Autowired
  private ProductPromotionService productPromotionService;

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private CartService cartService;

  /**
   * 從購物車創建訂單
   * @param userId 用戶ID
   * @param shippingAddress 配送地址
   * @return 訂單ID
   */
  @Transactional
  public int createOrderFromCart(int userId, String shippingAddress) {
    // 驗證基本信息
    validateAddress(shippingAddress);

    List<CartItems> cartItems = cartService.getCartItems(userId);
    if (cartItems.isEmpty()) {
      throw new IllegalStateException("購物車為空");
    }

    BigDecimal totalAmount = BigDecimal.ZERO;

    // 檢查並立即建立保留庫存
    for (CartItems cartItem : cartItems) {
      int productId = cartItem.getProductId();
      int quantity = cartItem.getQuantity();

      Product product = productRepository.findById(productId);
      if (product == null) {
        throw new IllegalStateException("商品不存在：" + productId);
      }

      // 檢查可用庫存
      if (product.getAvailableStock() < quantity) {
        throw new IllegalStateException(
            String.format("商品「%s」庫存不足，剩餘%d件",
                product.getName(),
                product.getAvailableStock()
            ));
      }

      // 更新保留庫存
//      try {
//        productRepository.updateLockedStock(productId, quantity);
//      } catch (RuntimeException e) {
//        throw new IllegalStateException(
//            String.format("商品「%s」庫存已被其他訂單保留，請重新確認",
//                product.getName()
//            ), e
//        );
//      }

      // 計算總額
      Map<String, Object> productPrice = productPromotionService.getProductWithPrice(productId);
      BigDecimal finalPrice = (BigDecimal) productPrice.get("promotionalPrice");
      totalAmount = totalAmount.add(finalPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    // 建立訂單
    Order order = new Order();
    order.setUserId(userId);
    order.setShippingAddress(shippingAddress);
    order.setStatusId(OrderStatusConstants.ORDERED);
    order.setTotalAmount(totalAmount);

    int orderId = orderRepository.create(order);

    // 建立訂單項目並鎖存庫
    for (CartItems cartItem : cartItems) {
      int productId = cartItem.getProductId();
      int quantity = cartItem.getQuantity();

      // 建立訂單項目
      Map<String, Object> productPrice = productPromotionService.getProductWithPrice(productId);
      BigDecimal finalPrice = (BigDecimal) productPrice.get("promotionalPrice");

      OrderItem orderItem = new OrderItem();
      orderItem.setOrderId(orderId);
      orderItem.setProductId(productId);
      orderItem.setQuantity(quantity);
      orderItem.setPrice(finalPrice);
      orderItemRepository.save(orderItem);

//      stockLockService.handleOrderCreated(orderId, productId, quantity, userId);
    }

    // 清空購物車
    cartService.clearCartItems(userId, cartItems.get(0).getCartId());

    return orderId;
  }

  /**
   * 處理信用卡支付
   * @param orderId 訂單ID
   * @param userId 用戶ID
   * @param cardNumber 信用卡號
   */
  @Transactional
  public void processCreditCardPayment(int orderId, int userId, String cardNumber) {
    Order order = validateOrderForPayment(orderId, userId);

    if (!paymentService.validateCreditCardPayment(cardNumber)) {
      throw new IllegalArgumentException("無效的信用卡號");
    }

    completePayment(orderId, "CREDIT_CARD");
  }

  /**
   * 處理 Apple Pay 支付
   * @param orderId 訂單ID
   * @param userId 用戶ID
   * @param applePayToken Apple Pay Token
   */
  @Transactional
  public void processApplePayPayment(int orderId, int userId, String applePayToken) {
    Order order = validateOrderForPayment(orderId, userId);

    if (!paymentService.validateApplePayPayment(applePayToken)) {
      throw new IllegalArgumentException("無效的 Apple Pay Token");
    }

    completePayment(orderId, "APPLE_PAY");
  }

  /**
   * 完成支付流程
   * @param orderId 訂單ID
   * @param paymentMethod 支付方式
   */
  private void completePayment(int orderId, String paymentMethod) {


    // 獲取訂單項目
    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

    // 扣除庫存
    for (OrderItem item : orderItems) {
      Product product = productRepository.findById(item.getProductId());
      if (product == null) {
        throw new RuntimeException("商品不存在");
      }

      // 檢查庫存是否足夠
      if (product.getStock() < item.getQuantity()) {
        throw new RuntimeException(
            String.format("商品「%s」庫存不足，剩餘%d件",
                product.getName(),
                product.getStock()
            ));
      }

      // 扣除庫存
      if (!productRepository.deductStock(item.getProductId(), item.getQuantity())) {
        throw new RuntimeException(
            String.format("商品「%s」庫存扣減失敗", product.getName()));
      }
    }
    //    stockLockService.handleOrderPaid(orderId);
    orderRepository.updateOrderStatus(orderId, OrderStatusConstants.PAID);
  }

  /**
   * 取消訂單
   * @param orderId 訂單ID
   * @param userId 用戶ID
   */
  @Transactional
  public void cancelOrder(int orderId, int userId) {
    validateOrderOwnership(orderId, userId);
    Order order = orderRepository.findById(orderId);

    if (order.getStatusId() != OrderStatusConstants.ORDERED) {
      throw new IllegalStateException("只有未付款的訂單可以取消");
    }

//    stockLockService.handleOrderCancelled(orderId);
    orderRepository.updateOrderStatus(orderId, OrderStatusConstants.CANCELLED);
  }

  /**
   * 查詢訂單
   * @param orderId 訂單ID
   * @param userId 用戶ID
   * @return 訂單信息
   */
  public Order getOrderById(int orderId, int userId) {
    Order order = orderRepository.findById(orderId);
    if (order == null || order.getUserId() != userId) {
      throw new IllegalArgumentException("訂單不存在");
    }

    List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
    order.setItems(items);

    return order;
  }

  /**
   * 查詢用戶的所有訂單
   * @param userId 用戶ID
   * @return 訂單列表
   */
  public List<Order> getUserOrders(int userId) {
    return orderRepository.findByUserId(userId);
  }

  /**
   * 驗證配送地址
   * @param address 配送地址
   */
  private void validateAddress(String address) {
    if (address == null || address.trim().isEmpty()) {
      throw new IllegalArgumentException("配送地址不能為空");
    }
    if (address.length() > 200) {
      throw new IllegalArgumentException("配送地址過長");
    }
  }

  /**
   * 驗證訂單所有權
   * @param orderId 訂單ID
   * @param userId 用戶ID
   */
  private void validateOrderOwnership(int orderId, int userId) {
    Order order = orderRepository.findById(orderId);
    if (order == null || order.getUserId() != userId) {
      throw new IllegalArgumentException("訂單不存在");
    }
  }

  /**
   * 驗證訂單支付條件
   * @param orderId 訂單ID
   * @param userId 用戶ID
   * @return 訂單實體
   */
  private Order validateOrderForPayment(int orderId, int userId) {
    log.info("開始驗證訂單支付 - 訂單ID: {}, 用戶ID: {}", orderId, userId);

    validateOrderOwnership(orderId, userId);
    Order order = orderRepository.findById(orderId);
    log.info("訂單資訊 - 狀態: {}, 建立時間: {}", order.getStatusId(), order.getCreatedAt());

    if (order.getStatusId() != OrderStatusConstants.ORDERED) {
      log.warn("訂單狀態不符 - 當前狀態: {}, 期望狀態: {}",
          order.getStatusId(), OrderStatusConstants.ORDERED);
      throw new IllegalStateException("訂單狀態不允許支付");
    }

    if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalStateException("訂單金額無效");
    }

    return order;
  }

  /**
   * 獲取所有訂單及其詳細內容
   * @return 訂單列表
   */
  public List<Order> getAllOrdersWithDetails() {
    List<Order> orders = orderRepository.findAll();
    for (Order order : orders) {
      List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
      order.setItems(items);
    }
    return orders;
  }



  /**
   * 獲取所有訂單及其詳細內容
   * 變更訂單狀態為被未付款取消
   */
  @Transactional
  public void processExpiredOrders() {
    // 查詢過期未付款訂單
    List<Order> expiredOrders = orderRepository.findExpiredUnpaidOrders();

    // 批次更新訂單狀態
    if (!expiredOrders.isEmpty()) {
      List<Integer> orderIds = expiredOrders.stream()
          .map(Order::getOrderId)
          .toList();

      orderRepository.updateExpiredOrderStatus(orderIds);
    }
  }
}
