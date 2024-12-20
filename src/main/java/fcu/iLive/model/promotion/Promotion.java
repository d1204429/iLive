//促銷活動實體

// src/main/java/fcu.iLive/model/promotion/Promotion.java
package fcu.iLive.model.promotion;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Promotion {
  private int promotionId;
  private String title;
  private String description;
  private String discountType;  // PERCENTAGE 或 FIXED_AMOUNT
  private BigDecimal discountValue;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}