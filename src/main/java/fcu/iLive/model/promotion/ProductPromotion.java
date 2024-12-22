//商品促銷實體

package fcu.iLive.model.promotion;

import java.time.LocalDateTime;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductPromotion {
  private int productPromotionId;
  private int productId;
  private int promotionId;
  private BigDecimal promotionalPrice;

  // 以下是從 Promotion 表關聯查詢出來的資料
  private String promotionTitle;      // 促銷活動標題
  private String discountType;        // 折扣類型
  private BigDecimal discountValue;   // 折扣值
  private LocalDateTime startDate;    // 活動開始時間
  private LocalDateTime endDate;      // 活動結束時間

}