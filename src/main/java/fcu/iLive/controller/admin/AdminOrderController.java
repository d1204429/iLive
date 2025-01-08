package fcu.iLive.controller.admin;

import fcu.iLive.model.order.Order;
import fcu.iLive.model.order.OrderStatusConstants;
import fcu.iLive.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 訂單管理員控制器
 * 處理所有訂單相關的後台管理API請求
 */
@RestController
@RequestMapping("/api/v1/admin/order")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

  private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

  @Autowired
  private OrderService orderService;

  private static final Set<Integer> ALLOWED_STATUS_CHANGES = Set.of(
      OrderStatusConstants.SHIPPED,
      OrderStatusConstants.CANCELLED,
      OrderStatusConstants.COMPLETED,
      OrderStatusConstants.REFUNDED,
      OrderStatusConstants.EXPIRED
  );

  /**
   * 獲取所有訂單列表（基本資訊）
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllOrders() {
    Map<String, Object> response = new HashMap<>();
    try {
      logger.info("開始獲取管理員訂單列表");
      List<Order> orders = orderService.findAllForAdmin();

      response.put("success", true);
      response.put("message", "獲取訂單列表成功");
      response.put("data", orders);

      logger.info("成功獲取管理員訂單列表，總數：{}", orders.size());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("獲取訂單列表失敗", e);
      response.put("success", false);
      response.put("message", "獲取訂單列表失敗：" + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 獲取訂單詳情（完整資訊）
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<Map<String, Object>> getOrderDetails(@PathVariable int orderId) {
    Map<String, Object> response = new HashMap<>();
    try {
      logger.info("開始獲取管理員訂單詳情，訂單ID：{}", orderId);

      Order order = orderService.findByIdForAdmin(orderId);
      if (order == null) {
        response.put("success", false);
        response.put("message", "找不到指定訂單");
        return ResponseEntity.badRequest().body(response);
      }

      response.put("success", true);
      response.put("message", "獲取訂單詳情成功");
      response.put("data", order);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("獲取訂單詳情失敗，訂單ID：{}", orderId, e);
      response.put("success", false);
      response.put("message", "獲取訂單詳情失敗：" + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 根據狀態獲取訂單列表（基本資訊）
   */
  @GetMapping("/status/{status}")
  public ResponseEntity<Map<String, Object>> getOrdersByStatus(@PathVariable int status) {
    Map<String, Object> response = new HashMap<>();
    try {
      logger.info("開始獲取狀態為 {} 的管理員訂單列表", status);

      List<Order> orders = orderService.findByStatusForAdmin(status);
      response.put("success", true);
      response.put("message", "獲取訂單列表成功");
      response.put("data", orders);

      logger.info("成功獲取狀態為 {} 的訂單，總數：{}", status, orders.size());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("獲取狀態為 {} 的訂單列表失敗", status, e);
      response.put("success", false);
      response.put("message", "獲取訂單列表失敗：" + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PutMapping("/{orderId}/status")
  public ResponseEntity<Map<String, Object>> updateOrderStatus(
      @PathVariable int orderId,
      @RequestBody Map<String, Integer> statusMap) {

    Map<String, Object> response = new HashMap<>();
    try {
      logger.info("開始更新訂單狀態，訂單ID：{}，請求內容：{}", orderId, statusMap);

      Integer newStatus = statusMap.get("statusId");
      if (newStatus == null) {
        throw new IllegalArgumentException("狀態值不能為空");
      }

      if (!ALLOWED_STATUS_CHANGES.contains(newStatus)) {
        logger.warn("嘗試更新為不允許的狀態值：{}，訂單ID：{}", newStatus, orderId);
        throw new IllegalArgumentException("不允許更改為該狀態值");
      }

      orderService.updateOrderStatus(orderId, newStatus);

      response.put("success", true);
      response.put("message", "訂單狀態更新成功");

      logger.info("成功更新訂單狀態，訂單ID：{}，新狀態：{}", orderId, newStatus);
      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      logger.warn("更新訂單狀態參數錯誤，訂單ID：{}", orderId, e);
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);

    } catch (Exception e) {
      logger.error("更新訂單狀態發生錯誤，訂單ID：{}", orderId, e);
      response.put("success", false);
      response.put("message", "更新訂單狀態失敗：" + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}