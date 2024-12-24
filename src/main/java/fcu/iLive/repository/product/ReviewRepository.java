package fcu.iLive.repository.product;

import fcu.iLive.model.product.Review;
import fcu.iLive.model.product.ReviewHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReviewRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  // 建立評論
  public void createReview(Review review) {
    String sql = """
           INSERT INTO Reviews (ProductID, UserID, Rating, Comment) 
           VALUES (?, ?, ?, ?)
       """;

    jdbcTemplate.update(sql,
        review.getProductId(),
        review.getUserId(),
        review.getRating(),
        review.getComment()
    );
  }

  // 更新評論
  @Transactional
  public void updateReview(Review review) {
    // 先記錄歷史
    String historySql = """
           INSERT INTO ReviewHistory 
           (ReviewID, ProductID, UserID, PreviousRating, PreviousComment, ChangeType)
           SELECT 
               ReviewID, ProductID, UserID, Rating, Comment, 'EDIT'
           FROM Reviews 
           WHERE ReviewID = ?
       """;
    jdbcTemplate.update(historySql, review.getReviewId());

    // 更新評論
    String updateSql = """
           UPDATE Reviews 
           SET Rating = ?, Comment = ?
           WHERE ReviewID = ?
       """;
    jdbcTemplate.update(updateSql,
        review.getRating(),
        review.getComment(),
        review.getReviewId()
    );
  }

  // 軟刪除評論
  @Transactional
  public void softDeleteReview(int reviewId) {
    // 先記錄歷史
    String historySql = """
           INSERT INTO ReviewHistory 
           (ReviewID, ProductID, UserID, PreviousRating, PreviousComment, ChangeType)
           SELECT 
               ReviewID, ProductID, UserID, Rating, Comment, 'DELETE'
           FROM Reviews 
           WHERE ReviewID = ?
       """;
    jdbcTemplate.update(historySql, reviewId);

    // 標記為刪除
    String deleteSql = """
           UPDATE Reviews 
           SET IsDeleted = TRUE, DeletedAt = CURRENT_TIMESTAMP
           WHERE ReviewID = ?
       """;
    jdbcTemplate.update(deleteSql, reviewId);
  }

  // 取得商品的評論列表
  public List<Review> getProductReviews(int productId) {
    String sql = """
           SELECT * FROM Reviews 
           WHERE ProductID = ? 
           AND IsDeleted = FALSE 
           ORDER BY CreatedAt DESC
       """;

    return jdbcTemplate.query(sql, new ReviewRowMapper(), productId);
  }

  // 取得特定評論
  public Review getReview(int reviewId) {
    String sql = """
           SELECT * FROM Reviews 
           WHERE ReviewID = ? 
           AND IsDeleted = FALSE
       """;

    List<Review> reviews = jdbcTemplate.query(sql, new ReviewRowMapper(), reviewId);
    return reviews.isEmpty() ? null : reviews.get(0);
  }

  // 取得評論歷史記錄
  public List<ReviewHistory> getReviewHistory(int reviewId) {
    String sql = """
           SELECT * FROM ReviewHistory 
           WHERE ReviewID = ? 
           ORDER BY ChangedAt DESC
       """;

    return jdbcTemplate.query(sql, new ReviewHistoryRowMapper());
  }

  // 檢查使用者是否已評論過商品
  public boolean hasUserReviewedProduct(int userId, int productId) {
    String sql = """
        SELECT COUNT(*) > 0 
        FROM Reviews 
        WHERE UserID = ? 
        AND ProductID = ?
        AND IsDeleted = FALSE  
    """;

    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject(sql, Boolean.class, userId, productId)
    );
  }

  // 檢查評論是否存在且未被刪除
  public boolean isReviewExistsAndActive(int reviewId, int userId) {
    String sql = """
           SELECT COUNT(*) > 0 
           FROM Reviews 
           WHERE ReviewID = ? 
           AND UserID = ? 
           AND IsDeleted = FALSE
       """;

    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject(sql, Boolean.class, reviewId, userId)
    );
  }

  // 取得平均評分
  public Double getAverageRating(int productId) {
    String sql = """
           SELECT AVG(Rating) 
           FROM Reviews 
           WHERE ProductID = ? 
           AND IsDeleted = FALSE
       """;

    return jdbcTemplate.queryForObject(sql, Double.class, productId);
  }

  // 取得評論數量
  public int getReviewCount(int productId) {
    String sql = """
           SELECT COUNT(*) 
           FROM Reviews 
           WHERE ProductID = ? 
           AND IsDeleted = FALSE
       """;

    return jdbcTemplate.queryForObject(sql, Integer.class, productId);
  }

  // Review RowMapper
  private static class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
      Review review = new Review();
      review.setReviewId(rs.getInt("ReviewID"));
      review.setProductId(rs.getInt("ProductID"));
      review.setUserId(rs.getInt("UserID"));
      review.setRating(rs.getDouble("Rating"));
      review.setComment(rs.getString("Comment"));
      review.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
      review.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
      review.setDeleted(rs.getBoolean("IsDeleted"));

      var deletedAt = rs.getTimestamp("DeletedAt");
      if (deletedAt != null) {
        review.setDeletedAt(deletedAt.toLocalDateTime());
      }

      return review;
    }
  }

  // ReviewHistory RowMapper
  private static class ReviewHistoryRowMapper implements RowMapper<ReviewHistory> {
    @Override
    public ReviewHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
      ReviewHistory history = new ReviewHistory();
      history.setHistoryId(rs.getInt("HistoryID"));
      history.setReviewId(rs.getInt("ReviewID"));
      history.setProductId(rs.getInt("ProductID"));
      history.setUserId(rs.getInt("UserID"));
      history.setPreviousRating(rs.getDouble("PreviousRating"));
      history.setPreviousComment(rs.getString("PreviousComment"));
      history.setChangeType(rs.getString("ChangeType"));
      history.setChangedAt(rs.getTimestamp("ChangedAt").toLocalDateTime());
      return history;
    }
  }
}