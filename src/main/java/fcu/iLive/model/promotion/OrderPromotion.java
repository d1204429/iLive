//訂單促銷實體

package fcu.iLive.model.promotion;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderPromotion {
  private int orderPromotionId;
  private int orderId;
  private int promotionId;
  private BigDecimal discountAmount;
}