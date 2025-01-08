package fcu.iLive.repository.product;

import fcu.iLive.model.product.RecommendedProduct;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RecommendedProductRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<RecommendedProduct> findAll() {
    String sql = "SELECT * FROM RecommendedProducts ORDER BY RecommendID";
    return jdbcTemplate.query(sql, (rs, rowNum) ->
        new RecommendedProduct(
            rs.getInt("RecommendID"),
            rs.getInt("ProductID"),
            rs.getTimestamp("CreatedAt").toLocalDateTime(),
            rs.getTimestamp("UpdatedAt").toLocalDateTime()
        )
    );
  }

  public RecommendedProduct findById(int recommendId) {
    String sql = "SELECT * FROM RecommendedProducts WHERE RecommendID = ?";
    return jdbcTemplate.queryForObject(sql, new Object[]{recommendId}, (rs, rowNum) ->
        new RecommendedProduct(
            rs.getInt("RecommendID"),
            rs.getInt("ProductID"),
            rs.getTimestamp("CreatedAt").toLocalDateTime(),
            rs.getTimestamp("UpdatedAt").toLocalDateTime()
        )
    );
  }

  public int insert(RecommendedProduct recommendedProduct) {
    String sql = "INSERT INTO RecommendedProducts (RecommendID, ProductID, CreatedAt, UpdatedAt) VALUES (?, ?, ?, ?)";
    return jdbcTemplate.update(sql,
        recommendedProduct.getRecommendId(),
        recommendedProduct.getProductId(),
        recommendedProduct.getCreatedAt(),
        recommendedProduct.getUpdatedAt()
    );
  }

  public int update(RecommendedProduct recommendedProduct) {
    String sql = "UPDATE RecommendedProducts SET ProductID = ?, UpdatedAt = ? WHERE RecommendID = ?";
    return jdbcTemplate.update(sql,
        recommendedProduct.getProductId(),
        recommendedProduct.getUpdatedAt(),
        recommendedProduct.getRecommendId()
    );
  }

  public int delete(int recommendId) {
    String sql = "DELETE FROM RecommendedProducts WHERE RecommendID = ?";
    return jdbcTemplate.update(sql, recommendId);
  }

  public int getMaxRecommendId() {
    String sql = "SELECT MAX(RecommendID) FROM RecommendedProducts";
    Integer maxId = jdbcTemplate.queryForObject(sql, Integer.class);
    return maxId != null ? maxId : 0;
  }
}