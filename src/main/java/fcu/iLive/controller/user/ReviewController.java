package fcu.iLive.controller.user;

import fcu.iLive.model.product.Review;
import fcu.iLive.model.product.ReviewHistory;
import fcu.iLive.service.product.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

  @Autowired
  private ReviewService reviewService;

  // 建立評論
  @PostMapping
  public ResponseEntity<?> createReview(@Valid @RequestBody Review review) {
    try {
      // 從 JWT token 取得使用者 ID
      org.springframework.security.core.Authentication auth =
          SecurityContextHolder.getContext().getAuthentication();
      int userId = Integer.parseInt(auth.getName());

      // 設置使用者 ID
      review.setUserId(userId);

      reviewService.createReview(review);
      return ResponseEntity.ok().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.status(403).body(Map.of(
          "message", e.getMessage()
      ));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of(
          "message", e.getMessage()
      ));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of(
              "message", "建立評論失敗: " + e.getMessage()
          ));
    }
  }

  @PutMapping("/{reviewId}")
  public ResponseEntity<?> updateReview(
      @PathVariable int reviewId,
      @Valid @RequestBody Review review) {
    try {
      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      int userId = Integer.parseInt(auth.getName());

      review.setReviewId(reviewId);
      review.setUserId(userId);
      reviewService.updateReview(review);
      return ResponseEntity.ok().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.status(403).body(Map.of(
          "message", e.getMessage()
      ));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of(
              "message", "更新評論失敗: " + e.getMessage()
          ));
    }
  }

  // 刪除評論
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<?> deleteReview(@PathVariable int reviewId) {
    try {
      org.springframework.security.core.Authentication auth =
          SecurityContextHolder.getContext().getAuthentication();
      int userId = Integer.parseInt(auth.getName());

      reviewService.deleteReview(reviewId, userId);
      return ResponseEntity.ok().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.status(403).body(Map.of(
          "message", e.getMessage()
      ));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of(
              "message", "刪除評論失敗: " + e.getMessage()
          ));
    }
  }

  // 取得商品評論列表
  @GetMapping("/product/{productId}")
  public ResponseEntity<?> getProductReviews(@PathVariable int productId) {
    try {
      List<Review> reviews = reviewService.getProductReviews(productId);
      return ResponseEntity.ok(reviews);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of(
              "message", "取得評論列表失敗: " + e.getMessage()
          ));
    }
  }

  // 取得單一評論
  @GetMapping("/{reviewId}")
  public ResponseEntity<?> getReview(@PathVariable int reviewId) {
    try {
      Review review = reviewService.getReview(reviewId);
      if (review == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(review);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of(
              "message", "取得評論失敗: " + e.getMessage()
          ));
    }
  }

  // 取得評論歷史記錄
  @GetMapping("/{reviewId}/history")
  public ResponseEntity<?> getReviewHistory(@PathVariable int reviewId) {
    try {
      List<ReviewHistory> history = reviewService.getReviewHistory(reviewId);
      return ResponseEntity.ok(history);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of(
              "message", "取得評論歷史失敗: " + e.getMessage()
          ));
    }
  }

  // 取得商品評分統計
  @GetMapping("/stats/{productId}")
  public ResponseEntity<?> getProductRatingStats(@PathVariable int productId) {
    try {
      Map<String, Object> stats = reviewService.getProductRatingStats(productId);
      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of(
              "message", "取得評分統計失敗: " + e.getMessage()
          ));
    }
  }
}