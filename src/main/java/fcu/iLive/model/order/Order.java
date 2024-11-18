//訂單實體

package fcu.iLive.model.order;

import fcu.iLive.model.user.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
  private int orderId;                // OrderID INT
  private int userId;                 // UserID INT
  private LocalDateTime orderDate;    // OrderDate DATETIME
  private BigDecimal totalAmount;     // TotalAmount DECIMAL(10,2)
  private String shippingAddress;     // ShippingAddress TEXT
  private String paymentMethod;       // PaymentMethod VARCHAR(50)
  private int statusId;               // StatusID INT
  private User user;                  // 關聯對象
  private OrderStatus status;         // 關聯對象

  // Constructor, Getters and Setters
}
