// src/main/java/fcu.iLive/repository/promotion/OrderPromotionRepository.java
package fcu.iLive.repository.promotion;

import fcu.iLive.model.promotion.OrderPromotion;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderPromotionRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 查詢訂單使用的優惠記錄
   * 包含優惠活動的詳細資訊，用於後台查詢及報表統計
   *
   * @param orderId 訂單ID
   * @return List<OrderPromotion> 訂單優惠使用記錄
   */
  public List<OrderPromotion> findByOrderId(Integer orderId) {
    String sql = """
            SELECT 
                op.*,
                p.Title as PromotionTitle,
                p.DiscountType,
                p.DiscountValue,
                p.StartDate,
                p.EndDate
            FROM OrderPromotions op
            JOIN Promotions p ON op.PromotionID = p.PromotionID
            WHERE op.OrderID = ?
            ORDER BY op.CreatedAt DESC
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      OrderPromotion promotion = new OrderPromotion();
      promotion.setOrderPromotionId(rs.getInt("OrderPromotionID"));
      promotion.setOrderId(rs.getInt("OrderID"));
      promotion.setPromotionId(rs.getInt("PromotionID"));
      promotion.setDiscountAmount(rs.getBigDecimal("DiscountAmount"));
      // 可以考慮加入更多資訊用於報表查詢
      return promotion;
    }, orderId);
  }

  /**
   * 記錄訂單使用的優惠活動
   * 在訂單成立時記錄使用了哪些優惠及折扣金額
   *
   * @param orderPromotion 訂單優惠使用記錄
   * @return Integer 紀錄ID
   */
  public Integer recordOrderPromotion(OrderPromotion orderPromotion) {
    String sql = """
            INSERT INTO OrderPromotions 
            (OrderID, PromotionID, DiscountAmount)
            VALUES (?, ?, ?)
        """;

    jdbcTemplate.update(sql,
        orderPromotion.getOrderId(),
        orderPromotion.getPromotionId(),
        orderPromotion.getDiscountAmount()
    );

    return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
  }

  /**
   * 查詢特定時間範圍內的優惠使用記錄
   * 用於後台統計分析
   *
   * @param startDate 開始日期
   * @param endDate 結束日期
   * @return List<OrderPromotion> 優惠使用記錄列表
   */
  public List<OrderPromotion> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    String sql = """
            SELECT 
                op.*,
                p.Title as PromotionTitle,
                o.OrderDate,
                o.TotalAmount
            FROM OrderPromotions op
            JOIN Promotions p ON op.PromotionID = p.PromotionID
            JOIN Orders o ON op.OrderID = o.OrderID
            WHERE o.OrderDate BETWEEN ? AND ?
            ORDER BY o.OrderDate DESC
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      OrderPromotion promotion = new OrderPromotion();
      promotion.setOrderPromotionId(rs.getInt("OrderPromotionID"));
      promotion.setOrderId(rs.getInt("OrderID"));
      promotion.setPromotionId(rs.getInt("PromotionID"));
      promotion.setDiscountAmount(rs.getBigDecimal("DiscountAmount"));
      // 設置其他需要的報表資訊
      return promotion;
    }, startDate, endDate);
  }

  /**
   * 統計優惠活動使用次數和總折扣金額
   * 用於分析優惠活動成效
   *
   * @param promotionId 優惠活動ID
   * @return Map<String, Object> 統計結果
   */
  public Map<String, Object> getPromotionUsageStats(Integer promotionId) {
    String sql = """
            SELECT 
                COUNT(*) as UseCount,
                SUM(DiscountAmount) as TotalDiscount,
                MIN(o.OrderDate) as FirstUseDate,
                MAX(o.OrderDate) as LastUseDate
            FROM OrderPromotions op
            JOIN Orders o ON op.OrderID = o.OrderID
            WHERE op.PromotionID = ?
        """;

    return jdbcTemplate.queryForMap(sql, promotionId);
  }
}