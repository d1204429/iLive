// 促銷活動管理 Controller
package fcu.iLive.controller.admin;

import fcu.iLive.model.promotion.Promotion;
import fcu.iLive.service.promotion.PromotionService;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/promotions")
public class PromotionController {

  @Autowired
  private PromotionService promotionService;

  /**
   * 取得所有有效的優惠活動
   */
  @GetMapping
  public ResponseEntity<List<Promotion>> getActivePromotions() {
    try {
      List<Promotion> promotions = promotionService.getAllActivePromotions();
      return new ResponseEntity<>(promotions, HttpStatus.OK);
    } catch (Exception e) {
      log.error("獲取促銷活動列表失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 取得單一優惠活動詳情
   */
  @GetMapping("/{id}")
  public ResponseEntity<Promotion> getPromotionDetails(@PathVariable("id") int promotionId) {
    try {
      Promotion promotion = promotionService.getPromotionById(promotionId);
      if (promotion != null) {
        return new ResponseEntity<>(promotion, HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      log.error("獲取促銷活動詳情失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 創建新的優惠活動
   */
  @PostMapping
  public ResponseEntity<Integer> createPromotion(@RequestBody Promotion promotion) {
    try {
      log.info("收到建立促銷活動請求: {}", promotion);
      int newPromotionId = promotionService.createPromotion(promotion);
      log.info("促銷活動建立成功，ID: {}", newPromotionId);
      return new ResponseEntity<>(newPromotionId, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      log.error("建立促銷活動參數錯誤: {}", e.getMessage());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("建立促銷活動失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 更新優惠活動
   */
  @PutMapping("/{id}")
  public ResponseEntity<Void> updatePromotion(
      @PathVariable("id") int promotionId,
      @RequestBody Promotion promotion) {
    try {
      promotion.setPromotionId(promotionId);
      promotionService.updatePromotion(promotion);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("更新促銷活動失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 更新促銷活動狀態
   */
  @PatchMapping("/{id}/status")
  public ResponseEntity<Void> updatePromotionStatus(
      @PathVariable("id") int promotionId,
      @RequestBody Map<String, Boolean> status) {
    try {
      Boolean isActive = status.get("isActive");
      if (isActive == null) {
        throw new IllegalArgumentException("狀態值不能為空");
      }
      promotionService.updatePromotionStatus(promotionId, isActive);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("更新促銷活動狀態失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


}