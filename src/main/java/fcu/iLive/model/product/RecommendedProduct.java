package fcu.iLive.model.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedProduct {
  private int recommendId;          // RecommendID INT (也是排序)
  private int productId;            // ProductID INT
  private LocalDateTime createdAt;  // CreatedAt DATETIME
  private LocalDateTime updatedAt;  // UpdatedAt DATETIME
}