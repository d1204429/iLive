//評價實體

package fcu.iLive.model.product;

import fcu.iLive.model.user.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
  private int reviewId;               // ReviewID INT
  private int productId;              // ProductID INT
  private int userId;                 // UserID INT
  private double rating;              // Rating DOUBLE
  private String comment;             // Comment TEXT
  private LocalDateTime createdAt;    // CreatedAt DATETIME
  private Product product;            // 關聯對象
  private User user;                  // 關聯對象

  // Constructor, Getters and Setters
}
