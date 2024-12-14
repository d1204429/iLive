package fcu.iLive.controller.user;

import fcu.iLive.model.order.Order;
import fcu.iLive.service.order.OrderService;
import fcu.iLive.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 訂單控制器
 * 處理所有訂單相關的前台API請求
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  @Autowired
  private OrderService orderService;

  @Autowired
  private JwtUtil jwtUtil;

  /**
   * 創建訂單
   * 從購物車創建新訂單，並自動鎖定庫存
   *
   * @param token JWT令牌
   * @param requestMap 請求參數，包含配送地址
   * @return 訂單創建結果
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createOrder(
      @RequestHeader("Authorization") String token,
      @RequestBody Map<String, String> requestMap) {

    Map<String, Object> response = new HashMap<>();
    try {
      // 驗證用戶
      int userId = jwtUtil.getUserIdFromToken(token.substring(7));

      // 獲取配送地址
      String shippingAddress = requestMap.get("shippingAddress");
      if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
        throw new IllegalArgumentException("請填寫配送地址");
      }

      // 創建訂單
      int orderId = orderService.createOrderFromCart(userId, shippingAddress);

      // 構建響應
      response.put("success", true);
      response.put("message", "訂單創建成功");
      response.put("data", Map.of(
          "orderId", orderId
      ));

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 處理訂單付款
   * 根據不同的付款方式進行相應的處理
   *
   * @param token JWT令牌
   * @param orderId 訂單ID
   * @param paymentInfo 付款信息，包含付款方式和相關驗證數據
   * @return 付款處理結果
   */
  @PostMapping("/{orderId}/payment")
  public ResponseEntity<Map<String, Object>> processPayment(
      @RequestHeader("Authorization") String token,
      @PathVariable int orderId,
      @RequestBody Map<String, String> paymentInfo) {

    Map<String, Object> response = new HashMap<>();
    try {
      // 解析用戶ID
      int userId = jwtUtil.getUserIdFromToken(token.substring(7));

      // 獲取並驗證付款方式
      String paymentMethod = paymentInfo.get("paymentMethod");
      if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
        throw new IllegalArgumentException("請選擇付款方式");
      }

      // 根據不同付款方式處理
      switch (paymentMethod.toUpperCase()) {
        case "CREDIT_CARD":
          String cardNumber = paymentInfo.get("cardNumber");
          if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("請輸入信用卡號");
          }
          orderService.processCreditCardPayment(orderId, userId, cardNumber);
          break;

        case "APPLE_PAY":
          String applePayToken = paymentInfo.get("applePayToken");
          if (applePayToken == null || applePayToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Apple Pay驗證資訊不完整");
          }
          orderService.processApplePayPayment(orderId, userId, applePayToken);
          break;

        default:
          throw new IllegalArgumentException("不支援的付款方式：" + paymentMethod);
      }

      response.put("success", true);
      response.put("message", "付款成功");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 取消訂單
   * 取消訂單並釋放庫存鎖定
   *
   * @param token JWT令牌
   * @param orderId 訂單ID
   * @return 取消結果
   */
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<Map<String, Object>> cancelOrder(
      @RequestHeader("Authorization") String token,
      @PathVariable int orderId) {

    Map<String, Object> response = new HashMap<>();
    try {
      int userId = jwtUtil.getUserIdFromToken(token.substring(7));
      orderService.cancelOrder(orderId, userId);

      response.put("success", true);
      response.put("message", "訂單取消成功");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 獲取訂單詳情
   *
   * @param token JWT令牌
   * @param orderId 訂單ID
   * @return 訂單詳細信息
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<Map<String, Object>> getOrder(
      @RequestHeader("Authorization") String token,
      @PathVariable int orderId) {

    Map<String, Object> response = new HashMap<>();
    try {
      int userId = jwtUtil.getUserIdFromToken(token.substring(7));
      Order order = orderService.getOrderById(orderId, userId);

      response.put("success", true);
      response.put("data", order);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 獲取用戶所有訂單
   *
   * @param token JWT令牌
   * @return 訂單列表
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getUserOrders(
      @RequestHeader("Authorization") String token) {

    Map<String, Object> response = new HashMap<>();
    try {
      int userId = jwtUtil.getUserIdFromToken(token.substring(7));
      List<Order> orders = orderService.getUserOrders(userId);

      response.put("success", true);
      response.put("data", Map.of(
          "orders", orders,
          "total", orders.size()
      ));

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}