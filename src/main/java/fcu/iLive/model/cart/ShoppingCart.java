//購物車實體

package fcu.iLive.model.cart;

import fcu.iLive.model.user.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCart {
  private int cartId;                 // CartID INT
  private int userId;                 // UserID INT
  private LocalDateTime createdAt;    // CreatedAt DATETIME
  private User user;                  // 關聯對象

  // Constructor, Getters and Setters
}