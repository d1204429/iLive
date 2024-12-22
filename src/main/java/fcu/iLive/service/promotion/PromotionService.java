// PromotionService.java
package fcu.iLive.service.promotion;

import fcu.iLive.model.promotion.Promotion;
import fcu.iLive.repository.promotion.PromotionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PromotionService {

  @Autowired
  private PromotionRepository promotionRepository;

  /**
   * 查詢所有當前有效的優惠活動
   */
  public List<Promotion> getAllActivePromotions() {
    return promotionRepository.findAllActive();
  }

  /**
   * 查詢特定優惠活動
   */
  public Promotion getPromotionById(int promotionId) {
    return promotionRepository.findById(promotionId);
  }

  /**
   * 建立新的優惠活動
   */
  @Transactional
  public int createPromotion(Promotion promotion) {
    validatePromotion(promotion);
    validatePromotionDates(promotion);
    validateDiscountSettings(promotion);
    return promotionRepository.create(promotion);
  }

  /**
   * 更新優惠活動
   */
  @Transactional
  public void updatePromotion(Promotion promotion) {
    validatePromotion(promotion);
    validatePromotionDates(promotion);
    validateDiscountSettings(promotion);
    promotionRepository.update(promotion);
  }

  /**
   * 更新促銷活動狀態
   */
  @Transactional
  public void updatePromotionStatus(int promotionId, Boolean isActive) {
    Promotion promotion = promotionRepository.findById(promotionId);
    if (promotion == null) {
      throw new IllegalArgumentException("找不到指定的促銷活動");
    }

    LocalDateTime now = LocalDateTime.now();
    if (Boolean.TRUE.equals(isActive)) {  // 當要啟用活動時
      // 只檢查是否已過期
      if (promotion.getEndDate().isBefore(now)) {
        throw new IllegalArgumentException("已過期的活動不能啟用");
      }
    }

    promotionRepository.updateStatus(promotionId, isActive);
  }
  /**
   * 停用優惠活動
   */
  @Transactional
  public void deactivatePromotion(int promotionId) {
    promotionRepository.deactivate(promotionId);
  }

  /**
   * 驗證優惠活動基本資訊
   */
  private void validatePromotion(Promotion promotion) {
    if (promotion == null) {
      throw new IllegalArgumentException("促銷活動資訊不能為空");
    }
    if (promotion.getTitle() == null || promotion.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("活動標題不能為空");
    }
    if (promotion.getDiscountType() == null) {
      throw new IllegalArgumentException("折扣類型不能為空");
    }
    if (!promotion.getDiscountType().equals("PERCENTAGE") &&
        !promotion.getDiscountType().equals("FIXED_AMOUNT")) {
      throw new IllegalArgumentException("無效的折扣類型");
    }
    if (promotion.getIsActive() == null) {
      throw new IllegalArgumentException("活動狀態不能為空");
    }
  }

  /**
   * 驗證活動時間
   */
  private void validatePromotionDates(Promotion promotion) {
    if (promotion.getStartDate() == null || promotion.getEndDate() == null) {
      throw new IllegalArgumentException("活動開始和結束時間不能為空");
    }

    LocalDateTime now = LocalDateTime.now();

    // 先檢查是否已結束
    if (promotion.getEndDate().isBefore(now)) {
      throw new IllegalArgumentException("無法修改已結束的活動");
    }

    // 再檢查時間邏輯正確性
    if (promotion.getEndDate().isBefore(promotion.getStartDate())) {
      throw new IllegalArgumentException("活動開始時間必須早於結束時間");
    }
  }

  /**
   * 驗證折扣設定
   */
  private void validateDiscountSettings(Promotion promotion) {
    if (promotion.getDiscountValue() == null) {
      throw new IllegalArgumentException("折扣值不能為空");
    }

    if (promotion.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("折扣值必須大於0");
    }

    if (promotion.getDiscountType().equals("PERCENTAGE")) {
      if (promotion.getDiscountValue().compareTo(BigDecimal.ONE) < 0 ||
          promotion.getDiscountValue().compareTo(new BigDecimal("99")) > 0) {
        throw new IllegalArgumentException("百分比折扣必須在1%到99%之間");
      }
    }
  }
}