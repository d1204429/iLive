//購物項目實體

package fcu.iLive.model.cart;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItems {
  private int cartItemId;
  private int cartId;
  private int productId;
  private int quantity;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  // getter和setter
}
