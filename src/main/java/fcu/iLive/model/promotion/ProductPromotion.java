//商品促銷實體

package fcu.iLive.model.promotion;

import fcu.iLive.model.product.Product;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPromotion {
  private int productPromotionId;     // ProductPromotionID INT
  private int productId;              // ProductID INT
  private int promotionId;            // PromotionID INT
  private BigDecimal promotionalPrice;// PromotionalPrice DECIMAL(10,2)
  private Product product;            // 關聯對象
  private Promotion promotion;        // 關聯對象

  // Constructor, Getters and Setters
}