//訂單項目實體

package fcu.iLive.model.order;

import fcu.iLive.model.product.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
  private int orderItemId;
  private int orderId;
  private int productId;
  private int quantity;
  private BigDecimal price;
  private LocalDateTime createdAt;

  // 關聯對象
  private Product product;
}