//訂單促銷實體

package fcu.iLive.model.promotion;

import fcu.iLive.model.order.Order;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPromotion {
  private int orderPromotionId;       // OrderPromotionID INT
  private int orderId;                // OrderID INT
  private int promotionId;            // PromotionID INT
  private BigDecimal discountAmount;  // DiscountAmount DECIMAL(10,2)
  private Order order;                // 關聯對象
  private Promotion promotion;        // 關聯對象

  // Constructor, Getters and Setters
}