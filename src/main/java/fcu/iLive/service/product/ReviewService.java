package fcu.iLive.service.product;

import fcu.iLive.model.product.Review;
import fcu.iLive.model.product.ReviewHistory;
import fcu.iLive.repository.product.ReviewRepository;
import fcu.iLive.repository.order.OrderRepository;
import fcu.iLive.util.XssUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private OrderRepository orderRepository;

  // 建立評論
  @Transactional
  public void createReview(Review review) {
    // 基本驗證
    validateReview(review,false);

    // 檢查是否購買過
    if (!orderRepository.hasUserPurchasedProduct(review.getUserId(), review.getProductId())) {
      throw new IllegalStateException("只有購買過商品且已收到商品或已退貨的會員才能評論");
    }

    // 檢查是否已評論過
    if (reviewRepository.hasUserReviewedProduct(review.getUserId(), review.getProductId())) {
      throw new IllegalStateException("您已經評論過此商品");
    }

    // XSS 防護
    review.setComment(XssUtils.sanitize(review.getComment()));

    // 建立評論
    reviewRepository.createReview(review);
  }

  // 更新評論
  @Transactional
  public void updateReview(Review review) {
    // 基本驗證
    validateReview(review,true);

    // 檢查評論是否存在且屬於該使用者
    if (!reviewRepository.isReviewExistsAndActive(review.getReviewId(), review.getUserId())) {
      throw new IllegalStateException("找不到評論或無權限修改");
    }

    // XSS 防護
    review.setComment(XssUtils.sanitize(review.getComment()));

    // 更新評論
    reviewRepository.updateReview(review);
  }

  // 刪除評論
  @Transactional
  public void deleteReview(int reviewId, int userId) {
    // 檢查評論是否存在且屬於該使用者
    if (!reviewRepository.isReviewExistsAndActive(reviewId, userId)) {
      throw new IllegalStateException("找不到評論或無權限刪除");
    }

    // 軟刪除評論
    reviewRepository.softDeleteReview(reviewId);
  }

  // 取得商品評論列表
  public List<Review> getProductReviews(int productId) {
    return reviewRepository.getProductReviews(productId);
  }

  // 取得特定評論
  public Review getReview(int reviewId) {
    return reviewRepository.getReview(reviewId);
  }

  // 取得評論歷史記錄
  public List<ReviewHistory> getReviewHistory(int reviewId) {
    return reviewRepository.getReviewHistory(reviewId);
  }

  // 取得商品評分統計
  public Map<String, Object> getProductRatingStats(int productId) {
    Map<String, Object> stats = new HashMap<>();

    Double avgRating = reviewRepository.getAverageRating(productId);
    int reviewCount = reviewRepository.getReviewCount(productId);

    stats.put("hasReviews", reviewCount > 0);
    stats.put("averageRating", avgRating != null ? avgRating : 0.0);
    stats.put("reviewCount", reviewCount);

    return stats;
  }

  // 評論驗證
  private void validateReview(Review review, boolean isUpdate) {
    if (review == null) {
      throw new IllegalArgumentException("評論資料不能為空");
    }
    if (!isUpdate && review.getProductId() <= 0) {  // 只在新增時檢查 productId
      throw new IllegalArgumentException("商品ID無效");
    }
    if (!isUpdate && review.getUserId() <= 0) {     // 只在新增時檢查 userId
      throw new IllegalArgumentException("使用者ID無效");
    }
    if (review.getRating() < 0 || review.getRating() > 5) {
      throw new IllegalArgumentException("評分必須在0到5之間");
    }
    if (review.getComment() == null || review.getComment().trim().isEmpty()) {
      throw new IllegalArgumentException("評論內容不能為空");
    }
    if (review.getComment().length() > 500) {
      throw new IllegalArgumentException("評論內容不能超過500字");
    }
  }
}