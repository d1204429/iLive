package fcu.iLive.service.promotion;

import fcu.iLive.model.promotion.OrderPromotion;
import fcu.iLive.repository.promotion.OrderPromotionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPromotionService {

  @Autowired
  private OrderPromotionRepository orderPromotionRepository;

  /**
   * 查詢訂單使用的優惠記錄
   *
   * @param orderId 訂單ID
   * @return List<OrderPromotion> 訂單優惠使用記錄
   */
  public List<OrderPromotion> getOrderPromotions(Integer orderId) {
    return orderPromotionRepository.findByOrderId(orderId);
  }

  /**
   * 記錄訂單使用的優惠
   *
   * @param orderPromotion 訂單優惠使用記錄
   * @return Integer 記錄ID
   */
  @Transactional
  public Integer recordOrderPromotion(OrderPromotion orderPromotion) {
    validateOrderPromotion(orderPromotion);
    return orderPromotionRepository.recordOrderPromotion(orderPromotion);
  }

  /**
   * 查詢指定時間範圍內的優惠使用記錄
   *
   * @param startDate 開始日期
   * @param endDate 結束日期
   * @return List<OrderPromotion> 優惠使用記錄列表
   */
  public List<OrderPromotion> getPromotionUsageByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    validateDateRange(startDate, endDate);
    return orderPromotionRepository.findByDateRange(startDate, endDate);
  }

  /**
   * 驗證訂單優惠記錄
   *
   * @param orderPromotion 訂單優惠記錄
   * @throws IllegalArgumentException 當記錄資訊不合理時
   */
  private void validateOrderPromotion(OrderPromotion orderPromotion) {
    if (orderPromotion.getOrderId() <= 0 || orderPromotion.getPromotionId() <= 0) {
      throw new IllegalArgumentException("訂單ID和優惠活動ID必須大於0");
    }

    if (orderPromotion.getDiscountAmount() == null ||
        orderPromotion.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("折扣金額不能為負數");
    }
  }

  /**
   * 驗證日期範圍
   *
   * @param startDate 開始日期
   * @param endDate 結束日期
   * @throws IllegalArgumentException 當日期範圍不合理時
   */
  private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("開始日期和結束日期不能為空");
    }

    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("結束日期不能早於開始日期");
    }
  }
}