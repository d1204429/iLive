//促銷服務實作
package fcu.iLive.service.promotion;

import fcu.iLive.model.promotion.Promotion;
import fcu.iLive.repository.promotion.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {

  @Autowired
  private PromotionRepository promotionRepository;

  /**
   * 查詢所有當前有效的優惠活動
   * 用於前台顯示可用優惠
   *
   * @return List<Promotion> 有效優惠活動列表
   */
  public List<Promotion> getAllActivePromotions() {
    return promotionRepository.findAllActive();
  }

  /**
   * 查詢特定優惠活動
   *
   * @param promotionId 優惠活動ID
   * @return Promotion 優惠活動資訊
   */
  public Promotion getPromotionById(Integer promotionId) {
    return promotionRepository.findById(promotionId);
  }

  /**
   * 建立新的優惠活動
   * 驗證活動時間範圍的合理性
   *
   * @param promotion 優惠活動資訊
   * @return Integer 新建的優惠活動ID
   * @throws IllegalArgumentException 當活動時間範圍不合理時
   */
  @Transactional
  public Integer createPromotion(Promotion promotion) {
    validatePromotionDates(promotion);
    return promotionRepository.create(promotion);
  }

  /**
   * 更新優惠活動資訊
   * 確保更新不會影響已使用該優惠的訂單
   *
   * @param promotion 優惠活動更新資訊
   * @throws IllegalArgumentException 當更新資訊不合理時
   */
  @Transactional
  public void updatePromotion(Promotion promotion) {
    validatePromotionDates(promotion);
    promotionRepository.update(promotion);
  }

  /**
   * 停用優惠活動
   * 不實際刪除記錄，僅標記為停用
   *
   * @param promotionId 優惠活動ID
   */
  @Transactional
  public void deactivatePromotion(Integer promotionId) {
    promotionRepository.deactivate(promotionId);
  }

  /**
   * 驗證優惠活動的時間範圍是否合理
   *
   * @param promotion 優惠活動資訊
   * @throws IllegalArgumentException 當時間範圍不合理時
   */
  private void validatePromotionDates(Promotion promotion) {
    LocalDateTime now = LocalDateTime.now();

    if (promotion.getStartDate().isBefore(now)) {
      throw new IllegalArgumentException("活動開始時間不能早於當前時間");
    }

    if (promotion.getEndDate().isBefore(promotion.getStartDate())) {
      throw new IllegalArgumentException("活動結束時間不能早於開始時間");
    }

    if (promotion.getEndDate().isBefore(now)) {
      throw new IllegalArgumentException("活動結束時間不能早於當前時間");
    }
  }
}
