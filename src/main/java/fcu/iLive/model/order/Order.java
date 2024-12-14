//訂單實體

package fcu.iLive.model.order;

import fcu.iLive.model.user.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
  private int orderId;
  private int userId;
  private LocalDateTime orderDate;
  private BigDecimal totalAmount;
  private String shippingAddress;
  private String paymentMethod;
  private int statusId;
  private LocalDateTime createdAt;

  // 關聯對象
  private User user;
  private OrderStatus status;
  private List<OrderItem> items;
}
