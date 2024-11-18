//促銷活動實體

package fcu.iLive.model.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Promotion {
  private int promotionId;            // PromotionID INT
  private String title;               // Title VARCHAR(100)
  private String description;         // Description TEXT
  private String discountType;        // DiscountType ENUM
  private BigDecimal discountValue;   // DiscountValue DECIMAL(10,2)
  private LocalDateTime startDate;    // StartDate DATETIME
  private LocalDateTime endDate;      // EndDate DATETIME
  private boolean isActive;           // IsActive BOOLEAN
  private LocalDateTime createdAt;    // CreatedAt DATETIME
  private LocalDateTime updatedAt;    // UpdatedAt DATETIME

  // Constructor, Getters and Setters
}