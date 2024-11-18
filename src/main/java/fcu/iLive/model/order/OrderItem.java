//訂單項目實體

package fcu.iLive.model.order;

import fcu.iLive.model.product.Product;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
  private int orderItemId;            // OrderItemID INT
  private int orderId;                // OrderID INT
  private int productId;              // ProductID INT
  private int quantity;               // Quantity INT
  private BigDecimal price;           // Price DECIMAL(10,2)
  private Order order;                // 關聯對象
  private Product product;            // 關聯對象

  // Constructor, Getters and Setters
}