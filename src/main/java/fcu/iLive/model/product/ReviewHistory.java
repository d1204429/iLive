package fcu.iLive.model.product;

import fcu.iLive.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewHistory {
  private int historyId;        // 歷史紀錄ID
  private int reviewId;         // 評論ID
  private int productId;        // 商品ID
  private int userId;           // 使用者ID
  private double previousRating;  // 變更前的評分
  private String previousComment; // 變更前的評論內容
  private String changeType;     // 變更類型：EDIT或DELETE
  private LocalDateTime changedAt; // 變更時間

  // 關聯物件
  private Review review;        // 關聯的評論
  private Product product;      // 關聯的商品
  private User user;           // 關聯的使用者
}