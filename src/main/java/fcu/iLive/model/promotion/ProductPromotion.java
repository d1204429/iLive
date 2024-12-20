//商品促銷實體

package fcu.iLive.model.promotion;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductPromotion {
  private int productPromotionId;
  private int productId;
  private int promotionId;
  private BigDecimal promotionalPrice;
}