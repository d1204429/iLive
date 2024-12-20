// src/main/java/fcu.iLive/repository/promotion/PromotionRepository.java
package fcu.iLive.repository.promotion;

import fcu.iLive.model.promotion.Promotion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PromotionRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 查詢所有當前有效的優惠活動
   * 條件:
   * 1. 活動狀態為啟用(IsActive = true)
   * 2. 當前時間在活動期間內(StartDate ~ EndDate)
   *
   * @return List<Promotion> 有效優惠活動列表
   */
  public List<Promotion> findAllActive() {
    String sql = """
           SELECT * FROM Promotions 
           WHERE IsActive = TRUE 
           AND CURRENT_TIMESTAMP BETWEEN StartDate AND EndDate
           ORDER BY CreatedAt DESC
       """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Promotion promotion = new Promotion();
      // 設置優惠活動ID
      promotion.setPromotionId(rs.getInt("PromotionID"));
      // 設置活動標題
      promotion.setTitle(rs.getString("Title"));
      // 設置活動描述
      promotion.setDescription(rs.getString("Description"));
      // 設置折扣類型(百分比或固定金額)
      promotion.setDiscountType(rs.getString("DiscountType"));
      // 設置折扣值
      promotion.setDiscountValue(rs.getBigDecimal("DiscountValue"));
      // 設置活動開始時間
      promotion.setStartDate(rs.getTimestamp("StartDate").toLocalDateTime());
      // 設置活動結束時間
      promotion.setEndDate(rs.getTimestamp("EndDate").toLocalDateTime());
      // 設置活動是否啟用
      promotion.setIsActive(rs.getBoolean("IsActive"));
      // 設置創建時間
      promotion.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
      // 設置更新時間
      promotion.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
      return promotion;
    });
  }

  /**
   * 根據ID查詢優惠活動
   *
   * @param promotionId 優惠活動ID
   * @return Promotion 優惠活動資訊
   */
  public Promotion findById(Integer promotionId) {
    String sql = "SELECT * FROM Promotions WHERE PromotionID = ?";

    try {
      return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
        Promotion promotion = new Promotion();
        promotion.setPromotionId(rs.getInt("PromotionID"));
        promotion.setTitle(rs.getString("Title"));
        promotion.setDescription(rs.getString("Description"));
        promotion.setDiscountType(rs.getString("DiscountType"));
        promotion.setDiscountValue(rs.getBigDecimal("DiscountValue"));
        promotion.setStartDate(rs.getTimestamp("StartDate").toLocalDateTime());
        promotion.setEndDate(rs.getTimestamp("EndDate").toLocalDateTime());
        promotion.setIsActive(rs.getBoolean("IsActive"));
        promotion.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        promotion.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
        return promotion;
      }, promotionId);
    } catch (Exception e) {
      // 如果找不到對應的優惠活動，返回null
      return null;
    }
  }

  /**
   * 新增優惠活動
   *
   * @param promotion 優惠活動資訊
   * @return Integer 新增的優惠活動ID
   */
  public Integer create(Promotion promotion) {
    String sql = """
           INSERT INTO Promotions (Title, Description, DiscountType, 
               DiscountValue, StartDate, EndDate, IsActive)
           VALUES (?, ?, ?, ?, ?, ?, ?)
       """;

    jdbcTemplate.update(sql,
        promotion.getTitle(),
        promotion.getDescription(),
        promotion.getDiscountType(),
        promotion.getDiscountValue(),
        promotion.getStartDate(),
        promotion.getEndDate(),
        promotion.getIsActive()
    );

    // 返回新增的優惠活動ID
    return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
  }

  /**
   * 更新優惠活動資訊
   *
   * @param promotion 優惠活動資訊
   */
  public void update(Promotion promotion) {
    String sql = """
           UPDATE Promotions 
           SET Title = ?, Description = ?, DiscountType = ?,
               DiscountValue = ?, StartDate = ?, EndDate = ?,
               IsActive = ?
           WHERE PromotionID = ?
       """;

    jdbcTemplate.update(sql,
        promotion.getTitle(),
        promotion.getDescription(),
        promotion.getDiscountType(),
        promotion.getDiscountValue(),
        promotion.getStartDate(),
        promotion.getEndDate(),
        promotion.getIsActive(),
        promotion.getPromotionId()
    );
  }

  /**
   * 停用優惠活動
   *
   * @param promotionId 優惠活動ID
   */
  public void deactivate(Integer promotionId) {
    String sql = "UPDATE Promotions SET IsActive = FALSE WHERE PromotionID = ?";
    jdbcTemplate.update(sql, promotionId);
  }
}