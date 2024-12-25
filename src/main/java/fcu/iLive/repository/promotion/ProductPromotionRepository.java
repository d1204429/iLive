package fcu.iLive.repository.promotion;

import fcu.iLive.model.promotion.ProductPromotion;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductPromotionRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 查詢特定商品的所有有效優惠
   * 條件：
   * 1. 指定商品ID
   * 2. 優惠活動狀態為啟用
   * 3. 當前時間在優惠期間內
   *
   * @param productId 商品ID
   * @return List<ProductPromotion> 商品優惠列表
   */
  public List<ProductPromotion> findByProductId(Integer productId) {
    String sql = """
            SELECT 
                pp.*,
                p.Title as PromotionTitle,
                p.DiscountType,
                p.DiscountValue,
                p.StartDate,
                p.EndDate
            FROM ProductPromotions pp
            JOIN Promotions p ON pp.PromotionID = p.PromotionID
            WHERE pp.ProductID = ?
            AND p.IsActive = TRUE
            AND CURRENT_TIMESTAMP BETWEEN p.StartDate AND p.EndDate
            ORDER BY pp.PromotionalPrice ASC
            """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      ProductPromotion promotion = new ProductPromotion();
      promotion.setProductPromotionId(rs.getInt("ProductPromotionID"));
      promotion.setProductId(rs.getInt("ProductID"));
      promotion.setPromotionId(rs.getInt("PromotionID"));
      promotion.setPromotionalPrice(rs.getBigDecimal("PromotionalPrice"));
      promotion.setPromotionTitle(rs.getString("PromotionTitle"));
      promotion.setDiscountType(rs.getString("DiscountType"));
      promotion.setDiscountValue(rs.getBigDecimal("DiscountValue"));
      return promotion;
    }, productId);
  }

  /**
   * 查詢特定商品促銷記錄
   *
   * @param productPromotionId 商品促銷ID
   * @return ProductPromotion 商品促銷資訊
   */
  public ProductPromotion findById(Integer productPromotionId) {
    String sql = """
            SELECT 
                pp.*,
                p.Title as PromotionTitle,
                p.DiscountType,
                p.DiscountValue
            FROM ProductPromotions pp
            JOIN Promotions p ON pp.PromotionID = p.PromotionID
            WHERE pp.ProductPromotionID = ?
            """;

    try {
      return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
        ProductPromotion promotion = new ProductPromotion();
        promotion.setProductPromotionId(rs.getInt("ProductPromotionID"));
        promotion.setProductId(rs.getInt("ProductID"));
        promotion.setPromotionId(rs.getInt("PromotionID"));
        promotion.setPromotionalPrice(rs.getBigDecimal("PromotionalPrice"));
        promotion.setPromotionTitle(rs.getString("PromotionTitle"));
        promotion.setDiscountType(rs.getString("DiscountType"));
        promotion.setDiscountValue(rs.getBigDecimal("DiscountValue"));
        return promotion;
      }, productPromotionId);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 查詢所有有效優惠商品ID
   * 條件：
   * 1. 商品必須有關聯的優惠活動
   * 2. 優惠活動狀態為啟用
   * 3. 當前時間在優惠期間內
   */
  public List<Integer> findActivePromotionProductIds() {
    String sql = """
            SELECT DISTINCT p.ProductID
            FROM Products p
            JOIN ProductPromotions pp ON p.ProductID = pp.ProductID
            JOIN Promotions prom ON pp.PromotionID = prom.PromotionID
            WHERE prom.IsActive = TRUE
                AND CURRENT_TIMESTAMP BETWEEN prom.StartDate AND prom.EndDate
            ORDER BY p.ProductID
            """;

    return jdbcTemplate.queryForList(sql, Integer.class);
  }

  /**
   * 新增商品優惠
   *
   * @param productPromotion 商品優惠資訊
   * @return Integer 新增的商品優惠ID
   */
  public Integer create(ProductPromotion productPromotion) {
    String sql = """
            INSERT INTO ProductPromotions 
            (ProductID, PromotionID, PromotionalPrice)
            VALUES (?, ?, ?)
            """;

    jdbcTemplate.update(sql,
        productPromotion.getProductId(),
        productPromotion.getPromotionId(),
        productPromotion.getPromotionalPrice()
    );

    return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
  }

  /**
   * 更新商品優惠價格
   *
   * @param productPromotionId 商品優惠ID
   * @param newPrice 新的優惠價格
   */
  public void updatePrice(Integer productPromotionId, BigDecimal newPrice) {
    String sql = "UPDATE ProductPromotions SET PromotionalPrice = ? WHERE ProductPromotionID = ?";
    jdbcTemplate.update(sql, newPrice, productPromotionId);
  }

  /**
   * 刪除商品優惠
   *
   * @param productPromotionId 商品優惠ID
   */
  public void delete(Integer productPromotionId) {
    String sql = "DELETE FROM ProductPromotions WHERE ProductPromotionID = ?";
    jdbcTemplate.update(sql, productPromotionId);
  }
}