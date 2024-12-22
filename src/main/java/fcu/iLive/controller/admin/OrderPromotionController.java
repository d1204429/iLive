
// 訂單促銷管理 Controller
package fcu.iLive.controller.admin;

import fcu.iLive.model.promotion.OrderPromotion;
import fcu.iLive.service.promotion.OrderPromotionService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/order-promotions")
public class OrderPromotionController {

  @Autowired
  private OrderPromotionService orderPromotionService;

  /**
   * 查詢訂單使用的優惠記錄
   */
  @GetMapping("/order/{orderId}")
  public ResponseEntity<List<OrderPromotion>> getOrderPromotions(
      @PathVariable("orderId") int orderId) {
    try {
      List<OrderPromotion> promotions = orderPromotionService.getOrderPromotions(orderId);
      return new ResponseEntity<>(promotions, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 記錄訂單使用的優惠
   */
  @PostMapping
  public ResponseEntity<Integer> recordOrderPromotion(@RequestBody OrderPromotion orderPromotion) {
    try {
      int recordId = orderPromotionService.recordOrderPromotion(orderPromotion);
      return new ResponseEntity<>(recordId, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 查詢時間範圍內的優惠使用記錄
   */
  @GetMapping("/usage")
  public ResponseEntity<List<OrderPromotion>> getPromotionUsage(
      @RequestParam LocalDateTime startDate,
      @RequestParam LocalDateTime endDate) {
    try {
      List<OrderPromotion> usages = orderPromotionService.getPromotionUsageByDateRange(startDate, endDate);
      return new ResponseEntity<>(usages, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}