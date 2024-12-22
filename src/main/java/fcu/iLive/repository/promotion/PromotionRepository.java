// PromotionRepository.java
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
   * 查詢所有優惠活動
   */
  public List<Promotion> findAllActive() {
    String sql = "SELECT * FROM Promotions ORDER BY PromotionID ASC";

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Promotion promotion = new Promotion();
      promotion.setPromotionId(rs.getInt("PromotionID"));
      promotion.setTitle(rs.getString("Title"));
      promotion.setDescription(rs.getString("Description"));
      promotion.setDiscountType(rs.getString("DiscountType"));
      promotion.setDiscountValue(rs.getBigDecimal("DiscountValue"));
      promotion.setStartDate(rs.getTimestamp("StartDate").toLocalDateTime());
      promotion.setEndDate(rs.getTimestamp("EndDate").toLocalDateTime());
      promotion.setIsActive(rs.getInt("IsActive") == 1);
      promotion.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
      promotion.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
      return promotion;
    });
  }

  /**
   * 根據ID查詢優惠活動
   */
  public Promotion findById(int promotionId) {
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
        promotion.setIsActive(rs.getInt("IsActive") == 1);
        promotion.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        promotion.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
        return promotion;
      }, promotionId);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 新增優惠活動
   */
  public int create(Promotion promotion) {
    String sql = """
            INSERT INTO Promotions (
                Title, Description, DiscountType, DiscountValue, 
                StartDate, EndDate, IsActive, CreatedAt, UpdatedAt
            ) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

    jdbcTemplate.update(sql,
        promotion.getTitle(),
        promotion.getDescription(),
        promotion.getDiscountType(),
        promotion.getDiscountValue(),
        promotion.getStartDate(),
        promotion.getEndDate(),
        promotion.getIsActive() ? 1 : 0
    );

    return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
  }

  /**
   * 更新優惠活動
   */
  public void update(Promotion promotion) {
    String sql = """
            UPDATE Promotions 
            SET Title = ?, 
                Description = ?, 
                DiscountType = ?,
                DiscountValue = ?, 
                StartDate = ?, 
                EndDate = ?,
                IsActive = ?,
                UpdatedAt = CURRENT_TIMESTAMP
            WHERE PromotionID = ?
            """;

    jdbcTemplate.update(sql,
        promotion.getTitle(),
        promotion.getDescription(),
        promotion.getDiscountType(),
        promotion.getDiscountValue(),
        promotion.getStartDate(),
        promotion.getEndDate(),
        promotion.getIsActive() ? 1 : 0,
        promotion.getPromotionId()
    );
  }

  /**
   * 更新促銷活動狀態
   */
  public void updateStatus(int promotionId, Boolean isActive) {
    String sql = """
            UPDATE Promotions 
            SET IsActive = ?, 
                UpdatedAt = CURRENT_TIMESTAMP 
            WHERE PromotionID = ?
            """;

    jdbcTemplate.update(sql, isActive ? 1 : 0, promotionId);
  }

  /**
   * 停用促銷活動
   */
  public void deactivate(int promotionId) {
    String sql = "UPDATE Promotions SET IsActive = 0 WHERE PromotionID = ?";
    jdbcTemplate.update(sql, promotionId);
  }
}